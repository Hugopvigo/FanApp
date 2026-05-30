package com.mediatracker.data.repository

import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.UserItemDao
import com.mediatracker.data.local.UserItemEntity
import com.mediatracker.data.local.toDomain
import com.mediatracker.data.local.toEntity
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userItemDao: UserItemDao,
    private val mediaItemDao: MediaItemDao,
    private val firestoreDataSource: FirestoreDataSource,
) : UserRepository {

    override fun getUserItemsFlow(): Flow<List<UserItem>> =
        userItemDao.getAll().map { entities ->
            entities.map { entity ->
                val domain = entity.toDomain()
                val mediaId = "${domain.mediaType.name.lowercase()}_${domain.apiId}"
                val mediaEntity = mediaItemDao.getById(mediaId)
                var enriched = domain
                if (domain.posterUrl.isNullOrBlank() && mediaEntity?.posterUrl?.isNotBlank() == true) {
                    enriched = enriched.copy(posterUrl = mediaEntity.posterUrl)
                }
                if (domain.title.isBlank() && mediaEntity?.title?.isNotBlank() == true) {
                    enriched = enriched.copy(title = mediaEntity.title)
                }
                enriched
            }
        }

    override suspend fun syncUserItems(): Result<Unit> = runCatching {
        firestoreDataSource.getUserItems().onSuccess { items ->
            items.forEach { remote ->
                val existing = userItemDao.getById(remote.id)
                when {
                    existing == null -> userItemDao.insert(remote.toEntity())
                    existing.updatedAt < remote.updatedAt -> {
                        val merged = mergeRemoteOverLocal(remote, existing.toDomain())
                        userItemDao.insert(merged.toEntity())
                    }
                    existing.updatedAt > remote.updatedAt -> {
                        firestoreDataSource.upsertUserItem(existing.toDomain())
                    }
                }
            }
        }.onFailure { Timber.w(it, "Firestore sync failed, using local data") }
    }

    override suspend fun addUserItem(
        mediaType: MediaType,
        apiId: String,
        title: String,
        posterUrl: String?,
        status: ItemStatus,
    ): Result<UserItem> = runCatching {
        val userItem = firestoreDataSource.addItem(mediaType, apiId, title, posterUrl, status).getOrThrow()
        userItemDao.insert(userItem.toEntity())
        userItem
    }.onFailure { Timber.e(it, "Add user item failed") }

    override suspend fun updateItemStatus(itemId: String, status: ItemStatus): Result<Unit> =
        updateEntity(itemId) { it.copy(status = status.name) }

    override suspend fun toggleFavorite(itemId: String): Result<Unit> = runCatching {
        val entity = userItemDao.getById(itemId)
            ?: throw NoSuchElementException("Item $itemId not found")
        if (entity.status == ItemStatus.ABANDONED.name) {
            throw IllegalStateException("Cannot favorite abandoned items")
        }
        updateEntity(itemId) { it.copy(favorite = !entity.favorite) }.getOrThrow()
    }.onFailure { Timber.e(it, "Toggle favorite failed: $itemId") }

    override suspend fun removeUserItem(itemId: String): Result<Unit> = runCatching {
        firestoreDataSource.removeItem(itemId)
        userItemDao.deleteById(itemId)
    }.onFailure { Timber.e(it, "Remove user item failed: $itemId") }

    override suspend fun updateUserRating(itemId: String, rating: Int?): Result<Unit> =
        updateEntity(itemId) { it.copy(userRating = rating) }

    override suspend fun updateUserNotes(itemId: String, notes: String): Result<Unit> =
        updateEntity(itemId) { it.copy(notes = notes) }

    override suspend fun updateSeasonEpisode(itemId: String, season: Int?, episode: Int?): Result<Unit> =
        updateEntity(itemId) {
            it.copy(currentSeason = season, currentEpisode = episode)
        }

    override suspend fun updatePageProgress(itemId: String, currentPage: Int?, totalPages: Int?): Result<Unit> =
        updateEntity(itemId) {
            it.copy(currentPage = currentPage, totalPages = totalPages)
        }

    override suspend fun updateWatchedEpisodes(itemId: String, watchedEpisodes: Map<Int, List<Int>>): Result<Unit> =
        runCatching {
            val json = if (watchedEpisodes.isEmpty()) "" else Json.encodeToString(watchedEpisodes)
            updateEntity(itemId) { it.copy(watchedEpisodes = json) }.getOrThrow()
        }.onFailure { Timber.e(it, "Update watched episodes failed: $itemId") }

    private suspend fun updateEntity(
        itemId: String,
        transform: (UserItemEntity) -> UserItemEntity,
    ): Result<Unit> = runCatching {
        val entity = userItemDao.getById(itemId)
            ?: throw NoSuchElementException("Item $itemId not found")
        val updated = transform(entity).copy(updatedAt = System.currentTimeMillis())
        userItemDao.insert(updated)
        firestoreDataSource.upsertUserItem(updated.toDomain()).getOrThrow()
    }.onFailure { Timber.e(it, "Update failed: $itemId") }

    private fun mergeRemoteOverLocal(remote: UserItem, local: UserItem): UserItem =
        remote.copy(
            posterUrl = remote.posterUrl ?: local.posterUrl,
            userRating = remote.userRating ?: local.userRating,
            notes = remote.notes ?: local.notes,
            currentSeason = remote.currentSeason ?: local.currentSeason,
            currentEpisode = remote.currentEpisode ?: local.currentEpisode,
            currentPage = remote.currentPage ?: local.currentPage,
            totalPages = remote.totalPages ?: local.totalPages,
            watchedEpisodes = remote.watchedEpisodes.ifEmpty { local.watchedEpisodes },
            title = remote.title.ifBlank { local.title.ifBlank { remote.apiId } },
        )
}
