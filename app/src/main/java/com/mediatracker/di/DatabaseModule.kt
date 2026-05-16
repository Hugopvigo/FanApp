package com.mediatracker.di

import android.content.Context
import androidx.room.Room
import com.mediatracker.data.local.AppDatabase
import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.UserItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mediatracker.db",
        ).build()

    @Provides
    fun provideMediaItemDao(database: AppDatabase): MediaItemDao =
        database.mediaItemDao()

    @Provides
    fun provideUserItemDao(database: AppDatabase): UserItemDao =
        database.userItemDao()
}
