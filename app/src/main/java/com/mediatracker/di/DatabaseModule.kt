package com.mediatracker.di

import android.content.Context
import androidx.room.Room
import com.mediatracker.data.local.AchievementDao
import com.mediatracker.data.local.AppDatabase
import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.StreakDao
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
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .build()

    @Provides
    fun provideMediaItemDao(database: AppDatabase): MediaItemDao =
        database.mediaItemDao()

    @Provides
    fun provideUserItemDao(database: AppDatabase): UserItemDao =
        database.userItemDao()

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao =
        database.notificationDao()

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = 
        database.achievementDao()

    @Provides
    fun provideStreakDao(database: AppDatabase): StreakDao = 
        database.streakDao()
}
