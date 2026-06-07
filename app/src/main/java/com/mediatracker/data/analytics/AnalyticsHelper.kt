package com.mediatracker.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val analytics: FirebaseAnalytics? = try {
        FirebaseAnalytics.getInstance(context)
    } catch (e: Exception) {
        Timber.w(e, "Firebase Analytics not available")
        null
    }

    fun logAddItem(mediaType: String, source: String = "search") {
        logEvent("add_item", Bundle().apply {
            putString("media_type", mediaType)
            putString("source", source)
        })
    }

    fun logCompleteItem(mediaType: String, rating: Int? = null) {
        logEvent("complete_item", Bundle().apply {
            putString("media_type", mediaType)
            if (rating != null) putInt("rating", rating)
        })
    }

    fun logStreakMilestone(days: Int) {
        logEvent("streak_milestone", Bundle().apply {
            putInt("days", days)
        })
    }

    fun logAchievementUnlocked(achievementId: String) {
        logEvent("achievement_unlocked", Bundle().apply {
            putString("achievement_id", achievementId)
        })
    }

    fun logEpisodeToggle(mediaType: String) {
        logEvent("episode_toggle", Bundle().apply {
            putString("media_type", mediaType)
        })
    }

    fun logPageProgress(progressPercent: Int) {
        logEvent("page_progress", Bundle().apply {
            putInt("progress_percent", progressPercent)
        })
    }

    fun logSearch(mediaType: String, queryLength: Int) {
        logEvent("search", Bundle().apply {
            putString("media_type", mediaType)
            putInt("query_length", queryLength)
        })
    }

    fun logScreenView(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    private fun logEvent(name: String, params: Bundle = Bundle()) {
        try {
            analytics?.logEvent(name, params)
        } catch (e: Exception) {
            Timber.w(e, "Failed to log analytics event: $name")
        }
    }
}
