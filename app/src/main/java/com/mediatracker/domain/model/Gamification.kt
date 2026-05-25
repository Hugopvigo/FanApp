package com.mediatracker.domain.model

data class UserLevel(
    val level: Int,
    val title: String,
    val icon: String,
    val totalXp: Int,
    val currentLevelXp: Int,
    val xpToNextLevel: Int,
)

data class UserRanking(
    val userId: String,
    val displayName: String,
    val avatarId: String?,
    val totalCompleted: Int,
    val xp: Int,
    val level: Int,
    val rank: Int,
    val yearStats: Map<Int, YearStat>,
)

data class YearStat(
    val year: Int,
    val seriesCompleted: Int,
    val moviesCompleted: Int,
    val booksCompleted: Int,
    val totalCompleted: Int,
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long?,
    val condition: AchievementCondition,
)

enum class AchievementCondition {
    ITEMS_COMPLETED_1,
    ITEMS_COMPLETED_5,
    ITEMS_COMPLETED_15,
    ITEMS_COMPLETED_30,
    ITEMS_COMPLETED_50,
    ITEMS_COMPLETED_100,
    FAVORITES_5,
    FAVORITES_15,
    ALL_MEDIA_TYPES,
    STREAK_7,
    STREAK_30,
}
