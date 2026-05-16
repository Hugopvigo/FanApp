package com.mediatracker.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    private fun userItemsPath(): String = "/users/${requireUserId()}/items"

    suspend fun getUserItems(): Result<List<UserItem>> = runCatching {
        val snapshot = firestore.collection(userItemsPath()).get().await()
        snapshot.documents.mapNotNull { doc ->
            try {
                UserItem(
                    id = doc.id,
                    mediaType = MediaType.valueOf(doc.getString("mediaType") ?: "SERIES"),
                    apiId = doc.getString("apiId") ?: "",
                    status = ItemStatus.valueOf(doc.getString("status") ?: "WATCHLIST"),
                    favorite = doc.getBoolean("favorite") ?: false,
                    addedAt = doc.getLong("addedAt") ?: 0L,
                    updatedAt = doc.getLong("updatedAt") ?: 0L,
                )
            } catch (e: Exception) {
                Timber.e(e, "Error parsing Firestore document ${doc.id}")
                null
            }
        }
    }

    suspend fun addItem(
        mediaType: MediaType,
        apiId: String,
        status: ItemStatus,
    ): Result<UserItem> = runCatching {
        val now = System.currentTimeMillis()
        val data = mapOf(
            "mediaType" to mediaType.name,
            "apiId" to apiId,
            "status" to status.name,
            "favorite" to false,
            "addedAt" to now,
            "updatedAt" to now,
        )
        val docRef = firestore.collection(userItemsPath()).add(data).await()
        UserItem(
            id = docRef.id,
            mediaType = mediaType,
            apiId = apiId,
            status = status,
            favorite = false,
            addedAt = now,
            updatedAt = now,
        )
    }

    suspend fun updateStatus(itemId: String, status: ItemStatus): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        firestore.collection(userItemsPath()).document(itemId)
            .update(mapOf("status" to status.name, "updatedAt" to now))
            .await()
    }

    suspend fun toggleFavorite(itemId: String, favorite: Boolean): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        firestore.collection(userItemsPath()).document(itemId)
            .update(mapOf("favorite" to favorite, "updatedAt" to now))
            .await()
    }

    suspend fun removeItem(itemId: String): Result<Unit> = runCatching {
        firestore.collection(userItemsPath()).document(itemId).delete().await()
    }
}
