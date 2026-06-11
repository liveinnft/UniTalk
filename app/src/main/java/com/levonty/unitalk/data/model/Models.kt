package com.levonty.unitalk.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.levonty.unitalk.data.local.Converters

// ───────────────────────────────────────────────
// User
// ───────────────────────────────────────────────

enum class Gender { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }

data class Language(
    val code: String,       // ISO 639-1, e.g. "en"
    val nameRu: String,
    val nameEn: String,
    val level: Int,         // 1–5, 0 = native
    val isNative: Boolean = false
)

data class Interest(
    val id: Long = 0,
    val nameRu: String,
    val nameEn: String,
    val namePl: String = "",
    val nameDe: String = "",
    val category: String = "",
    val usageCount: Int = 0
)

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey val id: String,
    val nickname: String,
    val birthDate: Long,            // epoch millis
    val gender: Gender? = null,
    val photoUrl: String? = null,
    val bio: String = "",
    val country: String = "",       // ISO 3166-1 alpha-2
    val city: String = "",
    val cityVisible: Boolean = true,
    val languages: List<Language> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0
) {
    val age: Int
        get() {
            val now = java.util.Calendar.getInstance()
            val birth = java.util.Calendar.getInstance().also { it.timeInMillis = birthDate }
            var age = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
            if (now.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) age--
            return age
        }
}

// ───────────────────────────────────────────────
// Message
// ───────────────────────────────────────────────

enum class MessageStatus { SENDING, SENT, DELIVERED, READ }

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val translatedText: String? = null,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)

// ───────────────────────────────────────────────
// Chat
// ───────────────────────────────────────────────

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val participantId: String,      // other user
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
)

// ───────────────────────────────────────────────
// Privacy settings
// ───────────────────────────────────────────────

enum class WhoCanMessage { EVERYONE, FRIENDS_ONLY, NOBODY }

data class PrivacySettings(
    val whoCanMessage: WhoCanMessage = WhoCanMessage.EVERYONE,
    val showCity: Boolean = true,
    val showOnlineStatus: Boolean = true
)