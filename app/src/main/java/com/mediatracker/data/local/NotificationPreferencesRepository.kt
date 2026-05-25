package com.mediatracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notifDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

@Singleton
class NotificationPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_PUSH = booleanPreferencesKey("push_enabled")
        private val KEY_NEW_RELEASES = booleanPreferencesKey("new_releases_enabled")
        private val KEY_RECOMMENDATIONS = booleanPreferencesKey("recommendations_enabled")
        private val KEY_WEEKLY_SUMMARY = booleanPreferencesKey("weekly_summary_enabled")
    }

    data class NotificationPreferences(
        val pushEnabled: Boolean = true,
        val newReleasesEnabled: Boolean = true,
        val recommendationsEnabled: Boolean = false,
        val weeklySummaryEnabled: Boolean = false,
    )

    val preferencesFlow: Flow<NotificationPreferences> = context.notifDataStore.data.map { prefs ->
        NotificationPreferences(
            pushEnabled = prefs[KEY_PUSH] ?: true,
            newReleasesEnabled = prefs[KEY_NEW_RELEASES] ?: true,
            recommendationsEnabled = prefs[KEY_RECOMMENDATIONS] ?: false,
            weeklySummaryEnabled = prefs[KEY_WEEKLY_SUMMARY] ?: false,
        )
    }

    suspend fun updatePushEnabled(enabled: Boolean) {
        context.notifDataStore.edit { it[KEY_PUSH] = enabled }
    }

    suspend fun updateNewReleasesEnabled(enabled: Boolean) {
        context.notifDataStore.edit { it[KEY_NEW_RELEASES] = enabled }
    }

    suspend fun updateRecommendationsEnabled(enabled: Boolean) {
        context.notifDataStore.edit { it[KEY_RECOMMENDATIONS] = enabled }
    }

    suspend fun updateWeeklySummaryEnabled(enabled: Boolean) {
        context.notifDataStore.edit { it[KEY_WEEKLY_SUMMARY] = enabled }
    }
}
