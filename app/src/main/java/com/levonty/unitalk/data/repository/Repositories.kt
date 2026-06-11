package com.levonty.unitalk.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.levonty.unitalk.data.local.dao.ChatDao
import com.levonty.unitalk.data.local.dao.MessageDao
import com.levonty.unitalk.data.local.dao.UserDao
import com.levonty.unitalk.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ─── Session (current logged-in user id) ───────────────────────────────────

private val KEY_CURRENT_USER = stringPreferencesKey("current_user_id")

@Singleton
class SessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val currentUserId: Flow<String?> = dataStore.data.map { it[KEY_CURRENT_USER] }

    suspend fun login(userId: String) {
        dataStore.edit { it[KEY_CURRENT_USER] = userId }
    }

    suspend fun logout() {
        dataStore.edit { it.remove(KEY_CURRENT_USER) }
    }
}

// ─── User ───────────────────────────────────────────────────────────────────

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getAllUsers(): Flow<List<User>> = userDao.getAllFlow()

    suspend fun getUserById(id: String): User? = userDao.getById(id)

    suspend fun saveUser(user: User) = userDao.upsert(user)

    /** Filter by age range and excluded countries; apply minor age-cap rule. */
    fun searchUsers(
        currentUser: User,
        ageFrom: Int,
        ageTo: Int,
        includeCountries: List<String>,  // empty = all
        excludeCountries: List<String>
    ): Flow<List<User>> {
        return userDao.getAllFlow().map { all ->
            all.filter { u ->
                if (u.id == currentUser.id) return@filter false
                // age range
                val age = u.age
                if (age < ageFrom || age > ageTo) return@filter false
                // countries
                if (includeCountries.isNotEmpty() && u.country !in includeCountries) return@filter false
                if (u.country in excludeCountries) return@filter false
                true
            }.sortedByDescending { u ->
                // simple score: common interests + common languages
                val commonInterests = u.interests.count { i ->
                    currentUser.interests.any { it.id == i.id }
                }
                val commonLangs = u.languages.count { l ->
                    currentUser.languages.any { it.code == l.code }
                }
                commonInterests * 2 + commonLangs
            }
        }
    }

    /** Compute allowed age range for minor safety rules. */
    fun safeAgeRange(userAge: Int): Pair<Int, Int> {
        return when {
            userAge <= 15 -> Pair(14, userAge + 5)
            userAge in 16..17 -> Pair(14, userAge + 10)
            else -> Pair(14, 99)
        }
    }
}

// ─── Messages / Chats ────────────────────────────────────────────────────────

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    fun getMessages(chatId: String): Flow<List<Message>> = messageDao.getMessages(chatId)
    fun getAllChats(): Flow<List<Chat>> = chatDao.getAllChats()

    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val msg = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        messageDao.upsert(msg)
        chatDao.upsert(
            Chat(
                id = chatId,
                participantId = chatId.replace(senderId, "").replace("_", ""),
                lastMessage = text,
                lastMessageTime = msg.timestamp
            )
        )
    }

    fun buildChatId(userId1: String, userId2: String): String =
        listOf(userId1, userId2).sorted().joinToString("_")
}