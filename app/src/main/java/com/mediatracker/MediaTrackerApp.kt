package com.mediatracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.StrictMode
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mediatracker.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MediaTrackerApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyLog()
                    .build(),
            )
            Timber.plant(Timber.DebugTree())
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
        }
        initializeFirebaseIfNeeded()
        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(64L * 1024 * 1024)
                .build()
        }
        .build()

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
