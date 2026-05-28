package com.mediatracker.domain.usecase

import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.NotificationEntity
import com.mediatracker.data.local.StreakDao
import com.mediatracker.data.local.StreakEntity
import com.mediatracker.domain.model.UserStreak
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    private val streakDao: StreakDao,
    private val notificationDao: NotificationDao,
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private data class Milestone(val days: Int, val xp: Int, val label: String)

    private val milestones = listOf(
        Milestone(7, 50, "Racha Semanal"),
        Milestone(30, 200, "Racha Mensual"),
        Milestone(100, 1000, "Racha de 100 días"),
        Milestone(365, 5000, "Racha de un año"),
    )

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

        val hitMilestones = if (entity.milestonesHit.isNotBlank()) {
            entity.milestonesHit.split(",").mapNotNull { it.toIntOrNull() }.toMutableList()
        } else {
            mutableListOf()
        }

        var addedXp = 0
        for (milestone in milestones) {
            if (newCurrent >= milestone.days && milestone.days !in hitMilestones) {
                hitMilestones.add(milestone.days)
                addedXp += milestone.xp
                val now = System.currentTimeMillis()
                notificationDao.insert(
                    NotificationEntity(
                        id = "streak_milestone_${milestone.days}_$now",
                        type = "STREAK_MILESTONE",
                        title = "🔥 ${milestone.label}",
                        body = "¡$milestone.days días consecutivos! +${milestone.xp} XP bonus",
                        createdAt = now,
                    )
                )
                Timber.i("Streak milestone hit: ${milestone.days} days, +${milestone.xp} XP")
            }
        }

        val newBonusXp = entity.bonusXp + addedXp
        val newMilestonesHit = hitMilestones.sorted().joinToString(",")

        val updated = entity.copy(
            currentStreak = newCurrent,
            longestStreak = newLongest,
            lastActiveDate = today,
            bonusXp = newBonusXp,
            milestonesHit = newMilestonesHit,
        )
        streakDao.upsert(updated)
        return UserStreak(newCurrent, newLongest, today, newBonusXp)
    }

    suspend fun getStreak(): UserStreak {
        val entity = streakDao.get() ?: return UserStreak(0, 0, "", 0)
        return UserStreak(entity.currentStreak, entity.longestStreak, entity.lastActiveDate, entity.bonusXp)
    }
}
