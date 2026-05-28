package com.mediatracker.data.fcm

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.mediatracker.data.firestore.FirebaseAuthProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenRepository @Inject constructor(
    private val messaging: FirebaseMessaging?,
    private val firestore: FirebaseFirestore?,
    private val authProvider: FirebaseAuthProvider,
) {
    suspend fun saveCurrentToken() {
        val m = messaging ?: return
        val uid = authProvider.userId ?: return
        val db = firestore ?: return
        runCatching {
            val token = m.token.await()
            db.document("/users/$uid").set(mapOf("fcmToken" to token), SetOptions.merge()).await()
            Timber.d("FCM token saved for uid=$uid")
        }.onFailure { Timber.e(it, "Failed to save FCM token") }
    }

    suspend fun saveToken(token: String) {
        val uid = authProvider.userId ?: return
        val db = firestore ?: return
        runCatching {
            db.document("/users/$uid").set(mapOf("fcmToken" to token), SetOptions.merge()).await()
            Timber.d("FCM token refreshed for uid=$uid")
        }.onFailure { Timber.e(it, "Failed to save refreshed FCM token") }
    }
}
