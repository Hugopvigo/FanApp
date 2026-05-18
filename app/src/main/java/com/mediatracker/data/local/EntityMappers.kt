package com.mediatracker.data.local

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.MediaItemWithUserStatus
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun MediaItemEntity.toDomain(): MediaItem = MediaItem(
    id = id,
    mediaType = MediaType.valueOf(mediaType),
    title = title,
    overview = overview,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    rating = rating,
    genres = if (genres.isBlank()) emptyList() else genres.split("|||"),
    extraData = if (extraData.isBlank()) null else json.decodeFromString<Map<String, String>>(extraData),
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
    mediaType = MediaType.valueOf(mediaType),
    apiId = apiId,
    title = title.ifBlank { apiId },
    posterUrl = posterUrl,
    status = ItemStatus.valueOf(status),
    favorite = favorite,
    addedAt = addedAt,
    updatedAt = updatedAt,
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
)

fun Pair<MediaItemEntity, UserItemEntity?>.toMediaItemWithUserStatus(): MediaItemWithUserStatus =
    MediaItemWithUserStatus(
        media = first.toDomain(),
        userStatus = second?.toDomain(),
    )
