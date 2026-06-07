package com.mediatracker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val mediaType: String,
    val apiId: String,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val releaseDate: String,
    val rating: Float,
    val genres: String,
    val extraData: String,
    val cachedAt: Long,
)

@Entity(tableName = "user_items")
data class UserItemEntity(
    @PrimaryKey val id: String,
    val mediaType: String,
    val apiId: String,
    val title: String,
    val posterUrl: String?,
    val status: String,
    val favorite: Boolean,
    val addedAt: Long,
    val updatedAt: Long,
    val userRating: Int? = null,
    val notes: String? = null,
    val currentSeason: Int? = null,
    val currentEpisode: Int? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val watchedEpisodes: String = "",
)

@Entity(
    tableName = "notifications",
    indices = [Index("isRead"), Index("createdAt")],
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: Long,
    val relatedApiId: String? = null,
    val relatedMediaType: String? = null,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val condition: String,
    val unlockedAt: Long? = null,
)

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val id: String = "user_streak",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String = "",
    val bonusXp: Int = 0,
    val milestonesHit: String = "",
)
