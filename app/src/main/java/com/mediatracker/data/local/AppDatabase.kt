package com.mediatracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MediaItemEntity::class, UserItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun userItemDao(): UserItemDao
}
