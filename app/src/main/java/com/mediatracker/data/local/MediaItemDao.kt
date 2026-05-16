package com.mediatracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MediaItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItemEntity)

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getById(id: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE mediaType = :mediaType")
    suspend fun getByType(mediaType: String): List<MediaItemEntity>

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' AND mediaType = :mediaType")
    suspend fun search(query: String, mediaType: String): List<MediaItemEntity>

    @Query("DELETE FROM media_items WHERE cachedAt < :timestamp")
    suspend fun deleteStale(timestamp: Long)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
