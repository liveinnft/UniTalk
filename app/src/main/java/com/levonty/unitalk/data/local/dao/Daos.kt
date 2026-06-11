package com.levonty.unitalk.data.local.dao

import androidx.room.*
import com.levonty.unitalk.data.model.Chat
import com.levonty.unitalk.data.model.Message
import com.levonty.unitalk.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): User?

    @Query("SELECT * FROM users")
    fun getAllFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE country IN (:countries) AND id != :currentUserId")
    fun filterByCountries(countries: List<String>, currentUserId: String): Flow<List<User>>
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessages(chatId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: Message)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chat: Chat)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markRead(chatId: String)
}