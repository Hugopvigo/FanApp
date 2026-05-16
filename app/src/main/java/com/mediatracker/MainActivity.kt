package com.mediatracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mediatracker.presentation.navigation.AppNavGraph
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

private const val PREF_THEME = "app_prefs"
private const val KEY_THEME   = "selected_theme"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(PREF_THEME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, AppTheme.Fantasy.name)
            ?.let { name -> AppTheme.entries.find { it.name == name } }
            ?: AppTheme.Fantasy

        setContent {
            var appTheme by remember { mutableStateOf(savedTheme) }

            MediaTrackerTheme(appTheme = appTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        onThemeChange = { theme ->
                            appTheme = theme
                            prefs.edit().putString(KEY_THEME, theme.name).apply()
                        },
                    )
                }
            }
        }
    }
}
