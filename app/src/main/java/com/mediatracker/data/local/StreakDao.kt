package com.mediatracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE id = 'user_streak' LIMIT 1")
    suspend fun get(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StreakEntity)
}
