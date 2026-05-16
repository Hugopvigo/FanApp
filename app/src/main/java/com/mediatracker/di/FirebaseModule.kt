package com.mediatracker.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp? {
        return try {
            FirebaseApp.getInstance()
        } catch (_: IllegalStateException) {
            Timber.w("FirebaseApp not initialized — Firebase features disabled")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp?): FirebaseAuth? {
        return firebaseApp?.let {
            try {
                FirebaseAuth.getInstance(it)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get FirebaseAuth")
                null
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp?): FirebaseFirestore? {
        return firebaseApp?.let {
            try {
                FirebaseFirestore.getInstance(it)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get FirebaseFirestore")
                null
            }
        }
    }
}
