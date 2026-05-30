package com.mediatracker.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(name = "locale_prefs")

/** `null` in flow means follow system. Stored values: `system`, `es`, `en`. */
@Singleton
class LocaleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        const val FOLLOW_SYSTEM = "system"
    }

    val languageCodeFlow: Flow<String> = context.localeDataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: FOLLOW_SYSTEM
    }

    suspend fun getLanguageCode(): String = languageCodeFlow.first()

    suspend fun setLanguageCode(code: String) {
        context.localeDataStore.edit { it[KEY_LANGUAGE] = code }
        applyLocales(code)
    }

    fun applyLocales(code: String) {
        val tags = when (code) {
            FOLLOW_SYSTEM -> ""
            "es" -> "es"
            "en" -> "en"
            else -> code
        }
        AppCompatDelegate.setApplicationLocales(
            if (tags.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tags),
        )
    }

    suspend fun applyStoredLocales() {
        applyLocales(getLanguageCode())
    }

    fun displayLabel(code: String): String = when (code) {
        FOLLOW_SYSTEM -> "system"
        "es" -> "es"
        "en" -> "en"
        else -> code
    }

    fun tmdbLanguage(code: String = FOLLOW_SYSTEM): String {
        val lang = if (code == FOLLOW_SYSTEM) {
            Locale.getDefault().language
        } else {
            code
        }
        return if (lang.startsWith("es")) "es-ES" else "en-US"
    }

    fun googleBooksLang(code: String = FOLLOW_SYSTEM): String {
        val lang = if (code == FOLLOW_SYSTEM) {
            Locale.getDefault().language
        } else {
            code
        }
        return when {
            lang.startsWith("es") -> "es"
            lang.startsWith("en") -> "en"
            else -> "es"
        }
    }
}
