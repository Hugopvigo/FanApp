package com.mediatracker.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.Ranking
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
        snapshot.documents.mapNotNull { doc -> parseUserItem(doc.id, doc.data) }
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
        val data = baseItemMap(
            mediaType = mediaType,
            apiId = apiId,
            title = title,
            posterUrl = posterUrl,
            status = status,
            favorite = false,
            addedAt = now,
            updatedAt = now,
        )
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

    suspend fun upsertUserItem(item: UserItem): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = "/users/$uid/items"
        val data = baseItemMap(
            mediaType = item.mediaType,
            apiId = item.apiId,
            title = item.title,
            posterUrl = item.posterUrl,
            status = item.status,
            favorite = item.favorite,
            addedAt = item.addedAt,
            updatedAt = item.updatedAt,
        ) + progressFieldsMap(item)
        db.collection(path).document(item.id)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .await()
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

    suspend fun updateRanking(
        displayName: String,
        avatarId: String?,
        totalCompleted: Int,
        seriesCompleted: Int,
        moviesCompleted: Int,
        booksCompleted: Int,
        xp: Int,
        level: Int,
    ): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val year = java.time.Year.now().value
        val data = mapOf(
            "userId" to uid,
            "displayName" to displayName,
            "avatarId" to (avatarId ?: ""),
            "totalCompleted" to totalCompleted,
            "seriesCompleted" to seriesCompleted,
            "moviesCompleted" to moviesCompleted,
            "booksCompleted" to booksCompleted,
            "xp" to xp,
            "level" to level,
            "updatedAt" to System.currentTimeMillis(),
        )
        db.document("/rankings/all_time/users/$uid").set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        db.document("/rankings/yearly/$year/users/$uid").set(data, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun getLeaderboard(category: String, limit: Int = 50): Result<List<Ranking>> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val path = when (category) {
            "yearly" -> "/rankings/yearly/${java.time.Year.now().value}/users"
            else -> "/rankings/all_time/users"
        }
        val orderField = when (category) {
            "series" -> "seriesCompleted"
            "movies" -> "moviesCompleted"
            "books" -> "booksCompleted"
            else -> "xp"
        }
        val snapshot = db.collection(path)
            .orderBy(orderField, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        snapshot.documents.mapIndexed { index, doc ->
            Ranking(
                id = doc.id,
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "Anonymous",
                avatarId = doc.getString("avatarId")?.ifBlank { null },
                xp = doc.getLong("xp")?.toInt() ?: 0,
                level = doc.getLong("level")?.toInt() ?: 1,
                totalCompleted = doc.getLong("totalCompleted")?.toInt() ?: 0,
                rank = index + 1,
            )
        }
    }

    suspend fun getUserRank(category: String): Result<Int?> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not configured")
        val uid = authProvider.userId ?: throw IllegalStateException("User not logged in")
        val path = when (category) {
            "yearly" -> "/rankings/yearly/${java.time.Year.now().value}/users"
            else -> "/rankings/all_time/users"
        }
        val snapshot = db.collection(path)
            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.indexOfFirst { it.id == uid }.takeIf { it >= 0 }?.plus(1)
    }

    private fun parseUserItem(id: String, data: Map<String, Any?>?): UserItem? = try {
        if (data == null) return null
        val apiId = data["apiId"] as? String ?: ""
        UserItem(
            id = id,
            mediaType = MediaType.valueOf(data["mediaType"] as? String ?: "SERIES"),
            apiId = apiId,
            title = (data["title"] as? String)?.ifBlank { null } ?: apiId,
            posterUrl = data["posterUrl"] as? String,
            status = ItemStatus.valueOf(data["status"] as? String ?: "WATCHLIST"),
            favorite = data["favorite"] as? Boolean ?: false,
            addedAt = (data["addedAt"] as? Number)?.toLong() ?: 0L,
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L,
            userRating = (data["userRating"] as? Number)?.toInt(),
            notes = data["notes"] as? String,
            currentSeason = (data["currentSeason"] as? Number)?.toInt(),
            currentEpisode = (data["currentEpisode"] as? Number)?.toInt(),
            currentPage = (data["currentPage"] as? Number)?.toInt(),
            totalPages = (data["totalPages"] as? Number)?.toInt(),
            watchedEpisodes = parseWatchedEpisodes(data["watchedEpisodes"] as? String),
        )
    } catch (e: Exception) {
        Timber.e(e, "Error parsing Firestore document $id")
        null
    }

    private fun parseWatchedEpisodes(raw: String?): Map<Int, List<Int>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            kotlinx.serialization.json.Json.decodeFromString<Map<Int, List<Int>>>(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun baseItemMap(
        mediaType: MediaType,
        apiId: String,
        title: String,
        posterUrl: String?,
        status: ItemStatus,
        favorite: Boolean,
        addedAt: Long,
        updatedAt: Long,
    ): MutableMap<String, Any> = mutableMapOf<String, Any>(
        "mediaType" to mediaType.name,
        "apiId" to apiId,
        "title" to title,
        "status" to status.name,
        "favorite" to favorite,
        "addedAt" to addedAt,
        "updatedAt" to updatedAt,
    ).apply {
        if (!posterUrl.isNullOrBlank()) this["posterUrl"] = posterUrl
    }

    private fun progressFieldsMap(item: UserItem): Map<String, Any?> = buildMap {
        item.userRating?.let { put("userRating", it) }
        item.notes?.let { put("notes", it) }
        item.currentSeason?.let { put("currentSeason", it) }
        item.currentEpisode?.let { put("currentEpisode", it) }
        item.currentPage?.let { put("currentPage", it) }
        item.totalPages?.let { put("totalPages", it) }
        if (item.watchedEpisodes.isNotEmpty()) {
            put("watchedEpisodes", kotlinx.serialization.json.Json.encodeToString(item.watchedEpisodes))
        }
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
