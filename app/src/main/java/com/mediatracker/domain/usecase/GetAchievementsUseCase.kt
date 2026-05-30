package com.mediatracker.domain.usecase

import com.mediatracker.data.local.AchievementDao
import com.mediatracker.data.local.StreakDao
import com.mediatracker.domain.model.ACHIEVEMENT_DEFS
import com.mediatracker.domain.model.Achievement
import com.mediatracker.domain.model.AchievementCondition
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val achievementDao: AchievementDao,
    private val streakDao: StreakDao,
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<List<Achievement>> = combine(
        achievementDao.getAll(),
        userRepository.getUserItemsFlow(),
    ) { entities, items ->
        val activeItems = items.filter { it.status != ItemStatus.ABANDONED }
        val completed = activeItems.filter { it.status == ItemStatus.COMPLETED }
        val seriesCompleted = completed.count { it.mediaType == MediaType.SERIES }
        val moviesCompleted = completed.count { it.mediaType == MediaType.MOVIE }
        val booksCompleted = completed.count { it.mediaType == MediaType.BOOK }
        val totalCompleted = completed.size
        val favorites = activeItems.count { it.favorite }
        val hasSeries = activeItems.any { it.mediaType == MediaType.SERIES }
        val hasMovies = activeItems.any { it.mediaType == MediaType.MOVIE }
        val hasBooks = activeItems.any { it.mediaType == MediaType.BOOK }
        val distinctTypes = listOf(hasSeries, hasMovies, hasBooks).count { it }
        val currentStreak = streakDao.get()?.currentStreak ?: 0

        ACHIEVEMENT_DEFS.map { def ->
            val entity = entities.find { it.id == def.id }
            val (progress, target) = computeProgress(def, totalItems = activeItems.size,
                totalCompleted = totalCompleted, seriesCompleted = seriesCompleted,
                moviesCompleted = moviesCompleted, booksCompleted = booksCompleted,
                favorites = favorites, distinctTypes = distinctTypes, currentStreak = currentStreak)
            Achievement(
                id = def.id,
                title = def.title,
                description = def.description,
                icon = def.icon,
                unlockedAt = entity?.unlockedAt,
                condition = def.condition,
                progress = progress,
                target = target,
            )
        }
    }

    private fun computeProgress(
        def: com.mediatracker.domain.model.AchievementDef,
        totalItems: Int,
        totalCompleted: Int,
        seriesCompleted: Int,
        moviesCompleted: Int,
        booksCompleted: Int,
        favorites: Int,
        distinctTypes: Int,
        currentStreak: Int,
    ): Pair<Int, Int> = when (def.condition) {
        AchievementCondition.FIRST_ADD -> minOf(totalItems, def.target) to def.target
        AchievementCondition.FIRST_COMPLETE -> minOf(totalCompleted, def.target) to def.target
        AchievementCondition.SERIES_FAN -> minOf(seriesCompleted, def.target) to def.target
        AchievementCondition.MOVIE_MARATHON -> minOf(moviesCompleted, def.target) to def.target
        AchievementCondition.BOOKWORM -> minOf(booksCompleted, def.target) to def.target
        AchievementCondition.COMPLETIONIST -> minOf(totalCompleted, def.target) to def.target
        AchievementCondition.CENTURION -> minOf(totalCompleted, def.target) to def.target
        AchievementCondition.EXPLORER -> minOf(distinctTypes, def.target) to def.target
        AchievementCondition.CURATOR -> minOf(favorites, def.target) to def.target
        AchievementCondition.DIVERSE -> minOf(minOf(seriesCompleted, moviesCompleted, booksCompleted), def.target) to def.target
        AchievementCondition.STREAK_7 -> minOf(currentStreak, def.target) to def.target
        AchievementCondition.STREAK_30 -> minOf(currentStreak, def.target) to def.target
    }
}
