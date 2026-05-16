package com.mediatracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserItemEntity)

    @Update
    suspend fun update(item: UserItemEntity)

    @Delete
    suspend fun delete(item: UserItemEntity)

    @Query("SELECT * FROM user_items ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<UserItemEntity>>

    @Query("SELECT * FROM user_items WHERE status = :status ORDER BY updatedAt DESC")
    fun getByStatus(status: String): Flow<List<UserItemEntity>>

    @Query("SELECT * FROM user_items WHERE mediaType = :mediaType ORDER BY updatedAt DESC")
    fun getByType(mediaType: String): Flow<List<UserItemEntity>>

    @Query("SELECT * FROM user_items WHERE status = :status AND mediaType = :mediaType ORDER BY updatedAt DESC")
    fun getByStatusAndType(status: String, mediaType: String): Flow<List<UserItemEntity>>

    @Query("SELECT * FROM user_items WHERE id = :id")
    suspend fun getById(id: String): UserItemEntity?

    @Query("DELETE FROM user_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
