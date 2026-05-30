package com.mediatracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

@Singleton
class OnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
    }

    suspend fun isOnboardingCompleted(): Boolean {
        val prefs = context.onboardingDataStore.data.first()
        return prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun markOnboardingCompleted() {
        context.onboardingDataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }
}
