package com.mediatracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.mediatracker.BuildConfig
import com.mediatracker.presentation.widget.WidgetWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MediaTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initializeFirebaseIfNeeded()
        createNotificationChannels()
        WidgetWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pushChannel = NotificationChannel(
            CHANNEL_PUSH,
            "FanApp Notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Push notifications from FanApp"
        }

        val achievementsChannel = NotificationChannel(
            CHANNEL_ACHIEVEMENTS,
            "Achievements",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Achievement unlocked notifications"
            enableVibration(true)
        }

        val streaksChannel = NotificationChannel(
            CHANNEL_STREAKS,
            "Streaks",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Streak milestone notifications"
            enableVibration(true)
        }

        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM,
            "System",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "System and app updates"
        }

        manager.createNotificationChannels(
            listOf(pushChannel, achievementsChannel, streaksChannel, systemChannel)
        )
    }

    companion object {
        const val CHANNEL_PUSH = "fanapp_push"
        const val CHANNEL_ACHIEVEMENTS = "fanapp_achievements"
        const val CHANNEL_STREAKS = "fanapp_streaks"
        const val CHANNEL_SYSTEM = "fanapp_system"
    }

    private fun initializeFirebaseIfNeeded() {
        try {
            FirebaseApp.getInstance()
            Timber.d("Firebase already initialized")
        } catch (_: IllegalStateException) {
            if (BuildConfig.TMDB_API_KEY.isBlank()) {
                Timber.w("No Firebase config found and no API keys — running in demo mode")
                return
            }
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(BuildConfig.APPLICATION_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Timber.d("Firebase initialized with manual config")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Firebase — cloud features disabled")
            }
        }
    }
}
