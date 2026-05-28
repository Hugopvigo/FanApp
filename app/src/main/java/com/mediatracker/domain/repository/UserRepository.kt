package com.mediatracker.domain.repository

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserItemsFlow(): Flow<List<UserItem>>
    suspend fun syncUserItems(): Result<Unit>
    suspend fun addUserItem(
        mediaType: MediaType,
        apiId: String,
        title: String,
        posterUrl: String?,
        status: ItemStatus,
    ): Result<UserItem>
    suspend fun updateItemStatus(itemId: String, status: ItemStatus): Result<Unit>
    suspend fun toggleFavorite(itemId: String): Result<Unit>
    suspend fun removeUserItem(itemId: String): Result<Unit>
    suspend fun updateUserRating(itemId: String, rating: Int?): Result<Unit>
    suspend fun updateUserNotes(itemId: String, notes: String): Result<Unit>
    suspend fun updateSeasonEpisode(itemId: String, season: Int?, episode: Int?): Result<Unit>
}
