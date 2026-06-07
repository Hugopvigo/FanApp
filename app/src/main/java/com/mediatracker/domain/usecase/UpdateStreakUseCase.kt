package com.mediatracker.domain.usecase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mediatracker.MainActivity
import com.mediatracker.MediaTrackerApp
import com.mediatracker.R
import com.mediatracker.data.analytics.AnalyticsHelper
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.NotificationEntity
import com.mediatracker.data.local.StreakDao
import com.mediatracker.data.local.StreakEntity
import com.mediatracker.domain.model.UserStreak
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streakDao: StreakDao,
    private val notificationDao: NotificationDao,
    private val analytics: AnalyticsHelper,
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
                showStreakNotification(milestone.label, milestone.days, milestone.xp)
                analytics.logStreakMilestone(milestone.days)
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

    private fun showStreakNotification(label: String, days: Int, xp: Int) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, label.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val body = "¡$days días consecutivos! +$xp XP bonus"
            val notification = NotificationCompat.Builder(context, MediaTrackerApp.CHANNEL_STREAKS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("🔥 $label")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(label.hashCode(), notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to show streak notification")
        }
    }
}
