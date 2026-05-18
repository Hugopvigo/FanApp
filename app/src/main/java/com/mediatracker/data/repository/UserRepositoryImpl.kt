package com.mediatracker.data.repository

import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.data.local.UserItemDao
import com.mediatracker.data.local.toDomain
import com.mediatracker.data.local.toEntity
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userItemDao: UserItemDao,
    private val firestoreDataSource: FirestoreDataSource,
) : UserRepository {

    override fun getUserItemsFlow(): Flow<List<UserItem>> =
        userItemDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncUserItems(): Result<Unit> = runCatching {
        firestoreDataSource.getUserItems().onSuccess { items ->
            items.forEach { item ->
                val existing = userItemDao.getById(item.id)
                if (existing == null || existing.updatedAt < item.updatedAt) {
                    userItemDao.insert(item.toEntity())
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
        runCatching {
            firestoreDataSource.updateStatus(itemId, status)
            val entity = userItemDao.getById(itemId)
                ?: throw NoSuchElementException("Item $itemId not found")
            userItemDao.insert(
                entity.copy(
                    status = status.name,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }.onFailure { Timber.e(it, "Update status failed: $itemId") }

    override suspend fun toggleFavorite(itemId: String): Result<Unit> = runCatching {
        val entity = userItemDao.getById(itemId)
            ?: throw NoSuchElementException("Item $itemId not found")
        if (entity.status == ItemStatus.ABANDONED.name) {
            throw IllegalStateException("Cannot favorite abandoned items")
        }
        val newFavorite = !entity.favorite
        firestoreDataSource.toggleFavorite(itemId, newFavorite)
        userItemDao.insert(
            entity.copy(
                favorite = newFavorite,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }.onFailure { Timber.e(it, "Toggle favorite failed: $itemId") }

    override suspend fun removeUserItem(itemId: String): Result<Unit> = runCatching {
        firestoreDataSource.removeItem(itemId)
        userItemDao.deleteById(itemId)
    }.onFailure { Timber.e(it, "Remove user item failed: $itemId") }
}
