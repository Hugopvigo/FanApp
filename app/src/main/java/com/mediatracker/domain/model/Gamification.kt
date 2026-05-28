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

data class UserStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: String,
    val bonusXp: Int = 0,
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long?,
    val condition: AchievementCondition,
    val progress: Int = 0,
    val target: Int = 1,
)

enum class AchievementCondition {
    FIRST_ADD,
    FIRST_COMPLETE,
    SERIES_FAN,
    MOVIE_MARATHON,
    BOOKWORM,
    COMPLETIONIST,
    CENTURION,
    EXPLORER,
    CURATOR,
    STREAK_7,
    STREAK_30,
    DIVERSE,
}

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val condition: AchievementCondition,
    val target: Int,
)

val ACHIEVEMENT_DEFS = listOf(
    AchievementDef("first_add", "Primer Paso", "Añade tu primer item", "🎯", AchievementCondition.FIRST_ADD, 1),
    AchievementDef("first_complete", "Mission Accomplished", "Completa tu primer item", "✅", AchievementCondition.FIRST_COMPLETE, 1),
    AchievementDef("series_fan", "Serie Adicta", "Completa 5 series", "📺", AchievementCondition.SERIES_FAN, 5),
    AchievementDef("movie_marathon", "Maratón de Cine", "Completa 10 películas", "🎬", AchievementCondition.MOVIE_MARATHON, 10),
    AchievementDef("bookworm", "Rata de Biblioteca", "Completa 5 libros", "📖", AchievementCondition.BOOKWORM, 5),
    AchievementDef("completionist", "Completista", "Completa 25 items", "🏅", AchievementCondition.COMPLETIONIST, 25),
    AchievementDef("centurion", "Centurión", "Completa 100 items", "🏛️", AchievementCondition.CENTURION, 100),
    AchievementDef("explorer", "Explorador", "Items de los 3 tipos", "🧭", AchievementCondition.EXPLORER, 3),
    AchievementDef("curator", "Curador", "10 favoritos", "💎", AchievementCondition.CURATOR, 10),
    AchievementDef("streak_7", "Racha Semanal", "7 días consecutivos", "🔥", AchievementCondition.STREAK_7, 7),
    AchievementDef("streak_30", "Imparable", "30 días consecutivos", "⚡", AchievementCondition.STREAK_30, 30),
    AchievementDef("diverse", "Polímata", "3 completados de cada tipo", "🌈", AchievementCondition.DIVERSE, 3),
)
