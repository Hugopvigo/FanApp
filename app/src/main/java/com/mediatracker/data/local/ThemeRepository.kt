package com.mediatracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mediatracker.presentation.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("selected_theme")
        private const val PREFS_LEGACY = "app_prefs"
        private const val KEY_LEGACY = "selected_theme"
    }

    val appThemeFlow: Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        prefs[KEY_THEME]?.let { name ->
            AppTheme.entries.find { it.name == name }
        } ?: AppTheme.Fantasy
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    fun migrateFromSharedPreferences() {
        val legacy = context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
        val saved = legacy.getString(KEY_LEGACY, null) ?: return
        val theme = AppTheme.entries.find { it.name == saved } ?: return
        legacy.edit().remove(KEY_LEGACY).apply()
        kotlinx.coroutines.runBlocking {
            setAppTheme(theme)
        }
    }
}
