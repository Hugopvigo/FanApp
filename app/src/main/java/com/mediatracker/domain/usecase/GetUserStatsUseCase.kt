package com.mediatracker.domain.usecase

import com.mediatracker.data.local.StreakDao
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.UserStats
import com.mediatracker.domain.model.UserLevel
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val streakDao: StreakDao,
) {
    operator fun invoke(): Flow<UserStats> =
        userRepository.getUserItemsFlow().map { items ->
            val activeItems = items.filter { it.status != ItemStatus.ABANDONED }
            val completedCount = activeItems.count { it.status == ItemStatus.COMPLETED }
            val inProgressCount = activeItems.count { it.status == ItemStatus.IN_PROGRESS }
            val favoriteCount = activeItems.count { it.favorite }
            val baseXp = calculateRetroactiveXp(items)
            val streakBonusXp = streakDao.get()?.bonusXp ?: 0
            val totalXp = baseXp + streakBonusXp
            val userLevel = calculateLevel(completedCount, totalXp)
            UserStats(
                seriesInProgress = activeItems.count { it.mediaType == MediaType.SERIES && it.status == ItemStatus.IN_PROGRESS },
                seriesCompleted = activeItems.count { it.mediaType == MediaType.SERIES && it.status == ItemStatus.COMPLETED },
                moviesInProgress = activeItems.count { it.mediaType == MediaType.MOVIE && it.status == ItemStatus.IN_PROGRESS },
                moviesCompleted = activeItems.count { it.mediaType == MediaType.MOVIE && it.status == ItemStatus.COMPLETED },
                booksInProgress = activeItems.count { it.mediaType == MediaType.BOOK && it.status == ItemStatus.IN_PROGRESS },
                booksCompleted = activeItems.count { it.mediaType == MediaType.BOOK && it.status == ItemStatus.COMPLETED },
                totalXp = totalXp,
                level = userLevel.level,
                levelTitle = userLevel.title,
                levelIcon = userLevel.icon,
            )
        }

    private fun calculateRetroactiveXp(items: List<UserItem>): Int {
        val activeItems = items.filter { it.status != ItemStatus.ABANDONED }
        val completedXp = activeItems.count { it.status == ItemStatus.COMPLETED } * XP_COMPLETED
        val inProgressXp = activeItems.count { it.status == ItemStatus.IN_PROGRESS } * XP_IN_PROGRESS
        val addedXp = activeItems.size * XP_ADD_ITEM
        val favoriteXp = activeItems.count { it.favorite } * XP_FAVORITE
        return completedXp + inProgressXp + addedXp + favoriteXp
    }

    private fun calculateLevel(completedCount: Int, totalXp: Int): UserLevel {
        val matched = LEVEL_TABLE.last { completedCount >= it.minItems }
        val nextIndex = LEVEL_TABLE.indexOf(matched) + 1
        val nextLevel = if (nextIndex < LEVEL_TABLE.size) LEVEL_TABLE[nextIndex] else null
        return UserLevel(
            level = matched.level,
            title = matched.title,
            icon = matched.icon,
            totalXp = totalXp,
            currentLevelXp = totalXp - matched.xpAtLevelStart,
            xpToNextLevel = nextLevel?.let { it.xpAtLevelStart - totalXp } ?: 0,
        )
    }

    private data class LevelDef(
        val level: Int,
        val title: String,
        val icon: String,
        val minItems: Int,
        val xpAtLevelStart: Int,
    )

    companion object {
        const val XP_ADD_ITEM = 5
        const val XP_COMPLETED = 25
        const val XP_IN_PROGRESS = 10
        const val XP_FAVORITE = 5
        const val XP_STREAK_BONUS = 50

        private val LEVEL_TABLE = listOf(
            LevelDef(1, "Novato", "\uD83C\uDF31", 0, 0),
            LevelDef(2, "Aficionado", "\uD83D\uDCDA", 5, 200),
            LevelDef(3, "Entusiasta", "\u2B50", 15, 600),
            LevelDef(4, "Experto", "\uD83C\uDFC6", 30, 1200),
            LevelDef(5, "Maestro", "\uD83D\uDC8E", 50, 2000),
            LevelDef(6, "Leyenda", "\uD83D\uDC51", 100, 4000),
        )
    }
}
