package com.mediatracker.domain.usecase

import com.mediatracker.data.local.AchievementDao
import com.mediatracker.data.local.AchievementEntity
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.NotificationEntity
import com.mediatracker.data.local.StreakDao
import com.mediatracker.domain.model.ACHIEVEMENT_DEFS
import com.mediatracker.domain.model.AchievementCondition
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val achievementDao: AchievementDao,
    private val notificationDao: NotificationDao,
    private val streakDao: StreakDao,
) {
    suspend operator fun invoke() {
        val items = try {
            userRepository.getUserItemsFlow().first()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load items for achievement check")
            return
        }

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

        for (def in ACHIEVEMENT_DEFS) {
            val existing = achievementDao.getById(def.id)
            if (existing?.unlockedAt != null) continue

            val (unlocked, progress) = evaluateCondition(
            condition = def.condition,
            target = def.target,
            totalItems = activeItems.size,
            totalCompleted = totalCompleted,
            seriesCompleted = seriesCompleted,
            moviesCompleted = moviesCompleted,
            booksCompleted = booksCompleted,
            favorites = favorites,
            distinctTypes = distinctTypes,
            currentStreak = currentStreak,
        )

            if (unlocked) {
                val now = System.currentTimeMillis()
                achievementDao.insert(
                    AchievementEntity(
                        id = def.id,
                        condition = def.condition.name,
                        unlockedAt = now,
                    )
                )
                notificationDao.insert(
                    NotificationEntity(
                        id = "ach_${def.id}_$now",
                        type = "ACHIEVEMENT_UNLOCKED",
                        title = "${def.icon} ${def.title}",
                        body = def.description,
                        createdAt = now,
                    )
                )
                Timber.i("Achievement unlocked: ${def.id}")
            } else if (existing == null) {
                achievementDao.insert(
                    AchievementEntity(
                        id = def.id,
                        condition = def.condition.name,
                        unlockedAt = null,
                    )
                )
            }
        }
    }

    private fun evaluateCondition(
        condition: AchievementCondition,
        target: Int,
        totalItems: Int,
        totalCompleted: Int,
        seriesCompleted: Int,
        moviesCompleted: Int,
        booksCompleted: Int,
        favorites: Int,
        distinctTypes: Int,
        currentStreak: Int = 0,
    ): Pair<Boolean, Int> = when (condition) {
        AchievementCondition.FIRST_ADD -> (totalItems >= target) to minOf(totalItems, target)
        AchievementCondition.FIRST_COMPLETE -> (totalCompleted >= target) to minOf(totalCompleted, target)
        AchievementCondition.SERIES_FAN -> (seriesCompleted >= target) to minOf(seriesCompleted, target)
        AchievementCondition.MOVIE_MARATHON -> (moviesCompleted >= target) to minOf(moviesCompleted, target)
        AchievementCondition.BOOKWORM -> (booksCompleted >= target) to minOf(booksCompleted, target)
        AchievementCondition.COMPLETIONIST -> (totalCompleted >= target) to minOf(totalCompleted, target)
        AchievementCondition.CENTURION -> (totalCompleted >= target) to minOf(totalCompleted, target)
        AchievementCondition.EXPLORER -> (distinctTypes >= target) to minOf(distinctTypes, target)
        AchievementCondition.CURATOR -> (favorites >= target) to minOf(favorites, target)
        AchievementCondition.DIVERSE -> {
            val minPerType = minOf(seriesCompleted, moviesCompleted, booksCompleted)
            (minPerType >= target) to minOf(minPerType, target)
        }
        AchievementCondition.STREAK_7 -> (currentStreak >= target) to minOf(currentStreak, target)
        AchievementCondition.STREAK_30 -> (currentStreak >= target) to minOf(currentStreak, target)
    }
}
