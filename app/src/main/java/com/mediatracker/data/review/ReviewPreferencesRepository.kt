package com.mediatracker.data.review

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reviewDataStore: DataStore<Preferences> by preferencesDataStore(name = "review_prefs")

@Singleton
class ReviewPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_LAST_PROMPT = longPreferencesKey("last_review_prompt_ms")
        private val KEY_COMPLETED_COUNT = longPreferencesKey("review_completed_count")
        private const val MIN_DAYS_BETWEEN = 90L
    }

    val lastPromptTime: Flow<Long> = context.reviewDataStore.data.map { it[KEY_LAST_PROMPT] ?: 0L }

    val completedCount: Flow<Int> = context.reviewDataStore.data.map { (it[KEY_COMPLETED_COUNT] ?: 0L).toInt() }

    suspend fun shouldShowReview(): Boolean {
        val prefs = context.reviewDataStore.data.first()
        val last = prefs[KEY_LAST_PROMPT] ?: 0L
        val count = (prefs[KEY_COMPLETED_COUNT] ?: 0L).toInt()
        val daysSinceLast = (System.currentTimeMillis() - last) / (24 * 60 * 60 * 1000)
        return count >= 3 && daysSinceLast >= MIN_DAYS_BETWEEN
    }

    suspend fun recordReviewPrompt() {
        context.reviewDataStore.edit {
            it[KEY_LAST_PROMPT] = System.currentTimeMillis()
            it[KEY_COMPLETED_COUNT] = 0L
        }
    }

    suspend fun incrementCompleted() {
        context.reviewDataStore.edit {
            val current = it[KEY_COMPLETED_COUNT] ?: 0L
            it[KEY_COMPLETED_COUNT] = current + 1
        }
    }
}
