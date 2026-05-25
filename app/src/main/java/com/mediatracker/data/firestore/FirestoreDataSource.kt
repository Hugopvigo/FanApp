package com.mediatracker.data.firestore

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
    private val firestore: FirebaseFirestore?,
    private val authProvider: FirebaseAuthProvider,
) {

    val isAvailable: Boolean
        get() = firestore != null && authProvider.userId != null

    suspend fun getUserItems(): Result<List<UserItem>> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        val snapshot = db.collection(path).get().await()
        snapshot.documents.mapNotNull { doc ->
            try {
                val apiId = doc.getString("apiId") ?: ""
                UserItem(
                    id = doc.id,
                    mediaType = MediaType.valueOf(doc.getString("mediaType") ?: "SERIES"),
                    apiId = apiId,
                    title = doc.getString("title")?.ifBlank { null } ?: apiId,
                    posterUrl = doc.getString("posterUrl"),
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
        title: String,
        posterUrl: String?,
        status: ItemStatus,
    ): Result<UserItem> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        val now = System.currentTimeMillis()
        val data = mutableMapOf(
            "mediaType" to mediaType.name,
            "apiId" to apiId,
            "title" to title,
            "status" to status.name,
            "favorite" to false,
            "addedAt" to now,
            "updatedAt" to now,
        )
        if (!posterUrl.isNullOrBlank()) data["posterUrl"] = posterUrl
        val docRef = db.collection(path).add(data).await()
        UserItem(
            id = docRef.id,
            mediaType = mediaType,
            apiId = apiId,
            title = title,
            posterUrl = posterUrl,
            status = status,
            favorite = false,
            addedAt = now,
            updatedAt = now,
        )
    }

    suspend fun updateStatus(itemId: String, status: ItemStatus): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        val now = System.currentTimeMillis()
        db.collection(path).document(itemId)
            .update(mapOf("status" to status.name, "updatedAt" to now))
            .await()
    }

    suspend fun toggleFavorite(itemId: String, favorite: Boolean): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        val now = System.currentTimeMillis()
        db.collection(path).document(itemId)
            .update(mapOf("favorite" to favorite, "updatedAt" to now))
            .await()
    }

    suspend fun removeItem(itemId: String): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        db.collection(path).document(itemId).delete().await()
    }

    suspend fun getAvatarId(): Result<String?> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        db.document("/users/$uid").get().await().getString("avatarId")
    }

    suspend fun updateAvatarId(avatarId: String): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        db.document("/users/$uid").set(mapOf("avatarId" to avatarId), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun getPrivacySettings(): Result<PrivacySettings> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val doc = db.document("/users/$uid/privacy/settings").get().await()
        PrivacySettings(
            publicProfile = doc.getBoolean("publicProfile") ?: true,
            showStats = doc.getBoolean("showStats") ?: true,
            showLibrary = doc.getBoolean("showLibrary") ?: true,
            shareActivity = doc.getBoolean("shareActivity") ?: false,
        )
    }

    suspend fun updatePrivacySettings(settings: PrivacySettings): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        db.document("/users/$uid/privacy/settings").set(settings.toMap()).await()
    }
}

data class PrivacySettings(
    val publicProfile: Boolean = true,
    val showStats: Boolean = true,
    val showLibrary: Boolean = true,
    val shareActivity: Boolean = false,
) {
    fun toMap(): Map<String, Boolean> = mapOf(
        "publicProfile" to publicProfile,
        "showStats" to showStats,
        "showLibrary" to showLibrary,
        "shareActivity" to shareActivity,
    )
}
