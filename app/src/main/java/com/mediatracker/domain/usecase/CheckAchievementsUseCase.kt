package com.mediatracker.domain.usecase

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mediatracker.MainActivity
import com.mediatracker.MediaTrackerApp
import com.mediatracker.R
import com.mediatracker.data.analytics.AnalyticsHelper
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val achievementDao: AchievementDao,
    private val notificationDao: NotificationDao,
    private val streakDao: StreakDao,
    private val analytics: AnalyticsHelper,
) {
    private val mutex = Mutex()

    suspend operator fun invoke() {
        mutex.withLock {
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
                    showAchievementNotification(def.icon, def.title, def.description)
                    analytics.logAchievementUnlocked(def.id)
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

    private fun showAchievementNotification(icon: String, title: String, description: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (!nm.areNotificationsEnabled()) return

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val notifId = title.toNotificationId()
            val pendingIntent = PendingIntent.getActivity(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, MediaTrackerApp.CHANNEL_ACHIEVEMENTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("$icon $title")
                .setContentText(description)
                .setStyle(NotificationCompat.BigTextStyle().bigText(description))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            nm.notify(notifId, notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to show achievement notification")
        }
    }

    private fun String.toNotificationId(): Int = fold(0) { acc, c -> acc * 31 + c.code }
}
