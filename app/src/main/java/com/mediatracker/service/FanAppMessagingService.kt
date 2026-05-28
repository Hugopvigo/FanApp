package com.mediatracker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mediatracker.MainActivity
import com.mediatracker.R
import com.mediatracker.data.fcm.FcmTokenRepository
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.NotificationEntity
import com.mediatracker.data.local.NotificationPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class FanAppMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationDao: NotificationDao
    @Inject lateinit var prefsRepo: NotificationPreferencesRepository
    @Inject lateinit var tokenRepo: FcmTokenRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNewToken(token: String) {
        Timber.d("FCM token refreshed")
        scope.launch { tokenRepo.saveToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        scope.launch { handleMessage(message) }
    }

    private suspend fun handleMessage(message: RemoteMessage) {
        val prefs = prefsRepo.preferencesFlow.first()
        if (!prefs.pushEnabled) return

        val type = message.data["type"] ?: if (message.notification != null) "GENERAL" else return
        if (!shouldShow(type, prefs)) return

        val title = message.data["title"] ?: message.notification?.title ?: return
        val body = message.data["body"] ?: message.notification?.body ?: return
        val relatedApiId = message.data["relatedApiId"]
        val relatedMediaType = message.data["relatedMediaType"]

        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        notificationDao.insert(
            NotificationEntity(
                id = id,
                type = type,
                title = title,
                body = body,
                createdAt = now,
                relatedApiId = relatedApiId,
                relatedMediaType = relatedMediaType,
            )
        )

        showSystemNotification(title, body, id.hashCode())
    }

    private fun shouldShow(
        type: String,
        prefs: NotificationPreferencesRepository.NotificationPreferences,
    ) = when (type) {
        "NEW_RELEASE" -> prefs.newReleasesEnabled
        "RECOMMENDATION" -> prefs.recommendationsEnabled
        "WEEKLY_SUMMARY" -> prefs.weeklySummaryEnabled
        else -> true
    }

    private fun showSystemNotification(title: String, body: String, notifId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    companion object {
        const val CHANNEL_ID = "fanapp_push"
        const val CHANNEL_NAME = "FanApp Notifications"
    }
}
