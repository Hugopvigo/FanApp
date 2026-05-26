package com.mediatracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MediaItemEntity::class, UserItemEntity::class, NotificationEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun userItemDao(): UserItemDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_items ADD COLUMN title TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_items ADD COLUMN posterUrl TEXT DEFAULT NULL")
            }
        }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS notifications (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                isRead INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                relatedApiId TEXT DEFAULT NULL,
                relatedMediaType TEXT DEFAULT NULL
            )
            """.trimIndent())
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_items ADD COLUMN userRating INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE user_items ADD COLUMN notes TEXT DEFAULT NULL")
        }
    }
    }
}
