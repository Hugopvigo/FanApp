package com.mediatracker.domain.usecase

import com.mediatracker.data.local.StreakDao
import com.mediatracker.data.local.StreakEntity
import com.mediatracker.domain.model.UserStreak
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    private val streakDao: StreakDao,
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend operator fun invoke(): UserStreak {
        val today = LocalDate.now().format(formatter)
        val entity = streakDao.get() ?: StreakEntity()

        val (newCurrent, newLongest) = when (entity.lastActiveDate) {
            today -> entity.currentStreak to entity.longestStreak
            LocalDate.now().minusDays(1).format(formatter) -> {
                val c = entity.currentStreak + 1
                c to maxOf(entity.longestStreak, c)
            }
            else -> 1 to maxOf(entity.longestStreak, 1)
        }

        val updated = entity.copy(
            currentStreak = newCurrent,
            longestStreak = newLongest,
            lastActiveDate = today,
        )
        streakDao.upsert(updated)
        return UserStreak(newCurrent, newLongest, today)
    }

    suspend fun getStreak(): UserStreak {
        val entity = streakDao.get() ?: return UserStreak(0, 0, "")
        return UserStreak(entity.currentStreak, entity.longestStreak, entity.lastActiveDate)
    }
}
