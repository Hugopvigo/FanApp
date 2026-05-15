package com.mediatracker.domain.model

data class UserItem(
    val id: String,
    val mediaType: MediaType,
    val apiId: String,
    val status: ItemStatus,
    val favorite: Boolean,
    val addedAt: Long,
    val updatedAt: Long,
)

data class MediaItemWithUserStatus(
    val media: MediaItem,
    val userStatus: UserItem?,
)
