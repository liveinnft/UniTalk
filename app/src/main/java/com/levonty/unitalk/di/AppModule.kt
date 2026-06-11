package com.levonty.unitalk.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.levonty.unitalk.data.local.UniTalkDatabase
import com.levonty.unitalk.data.local.dao.ChatDao
import com.levonty.unitalk.data.local.dao.MessageDao
import com.levonty.unitalk.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unitalk_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): UniTalkDatabase =
        Room.databaseBuilder(ctx, UniTalkDatabase::class.java, "unitalk.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: UniTalkDatabase): UserDao = db.userDao()
    @Provides fun provideMessageDao(db: UniTalkDatabase): MessageDao = db.messageDao()
    @Provides fun provideChatDao(db: UniTalkDatabase): ChatDao = db.chatDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore
}