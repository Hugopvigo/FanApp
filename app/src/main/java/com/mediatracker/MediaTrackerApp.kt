package com.mediatracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.mediatracker.BuildConfig
import com.mediatracker.service.FanAppMessagingService
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
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            FanAppMessagingService.CHANNEL_ID,
            FanAppMessagingService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Push notifications from FanApp"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
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
