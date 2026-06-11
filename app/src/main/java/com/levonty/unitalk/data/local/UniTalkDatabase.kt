package com.levonty.unitalk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.levonty.unitalk.data.local.dao.ChatDao
import com.levonty.unitalk.data.local.dao.MessageDao
import com.levonty.unitalk.data.local.dao.UserDao
import com.levonty.unitalk.data.model.Chat
import com.levonty.unitalk.data.model.Message
import com.levonty.unitalk.data.model.User

@Database(
    entities = [User::class, Message::class, Chat::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UniTalkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
}