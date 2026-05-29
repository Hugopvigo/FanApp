package com.mediatracker.domain.model

data class UserItem(
    val id: String,
    val mediaType: MediaType,
    val apiId: String,
    val title: String,
    val posterUrl: String?,
    val status: ItemStatus,
    val favorite: Boolean,
    val addedAt: Long,
    val updatedAt: Long,
    val userRating: Int? = null,
    val notes: String? = null,
    val currentSeason: Int? = null,
    val currentEpisode: Int? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
)

data class MediaItemWithUserStatus(
    val media: MediaItem,
    val userStatus: UserItem?,
)
