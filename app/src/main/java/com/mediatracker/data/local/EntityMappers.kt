package com.mediatracker.data.local

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.MediaItemWithUserStatus
import kotlinx.serialization.json.Json
import timber.log.Timber

private val json = Json { ignoreUnknownKeys = true }

fun MediaItemEntity.toDomain(): MediaItem = MediaItem(
    id = id,
    mediaType = runCatching { MediaType.valueOf(mediaType) }.getOrElse { e ->
        Timber.e(e, "Unknown mediaType '$mediaType' for cached item $id, defaulting to MOVIE")
        MediaType.MOVIE
    },
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    rating = rating,
    genres = if (genres.isBlank()) emptyList() else genres.split("|||"),
    extraData = if (extraData.isBlank()) null else runCatching {
        json.decodeFromString<Map<String, String>>(extraData)
    }.onFailure { Timber.e(it, "Failed to parse extraData for item $id") }.getOrNull(),
)

fun MediaItem.toEntity(cachedAt: Long = System.currentTimeMillis()): MediaItemEntity = MediaItemEntity(
    id = id,
    mediaType = mediaType.name,
    apiId = id.removePrefix("${mediaType.name.lowercase()}_"),
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    rating = rating,
    genres = genres.joinToString("|||"),
    extraData = extraData?.let { json.encodeToString(it) } ?: "",
    cachedAt = cachedAt,
)

fun UserItemEntity.toDomain(): UserItem = UserItem(
    id = id,
    mediaType = runCatching { MediaType.valueOf(mediaType) }.getOrElse { e ->
        Timber.e(e, "Unknown mediaType '$mediaType' for user item $id, defaulting to MOVIE")
        MediaType.MOVIE
    },
    apiId = apiId,
    title = title.ifBlank { apiId },
    posterUrl = posterUrl,
    status = runCatching { ItemStatus.valueOf(status) }.getOrElse { e ->
        Timber.e(e, "Unknown status '$status' for user item $id, defaulting to WATCHLIST")
        ItemStatus.WATCHLIST
    },
    favorite = favorite,
    addedAt = addedAt,
    updatedAt = updatedAt,
    userRating = userRating,
    notes = notes,
    currentSeason = currentSeason,
    currentEpisode = currentEpisode,
    currentPage = currentPage,
    totalPages = totalPages,
)

fun UserItem.toEntity(): UserItemEntity = UserItemEntity(
    id = id,
    mediaType = mediaType.name,
    apiId = apiId,
    title = title,
    posterUrl = posterUrl,
    status = status.name,
    favorite = favorite,
    addedAt = addedAt,
    updatedAt = updatedAt,
    userRating = userRating,
    notes = notes,
    currentSeason = currentSeason,
    currentEpisode = currentEpisode,
    currentPage = currentPage,
    totalPages = totalPages,
)

fun Pair<MediaItemEntity, UserItemEntity?>.toMediaItemWithUserStatus(): MediaItemWithUserStatus =
    MediaItemWithUserStatus(
        media = first.toDomain(),
        userStatus = second?.toDomain(),
    )
