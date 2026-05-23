package com.mediatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.mediatracker.data.local.ThemeRepository
import com.mediatracker.presentation.navigation.AppNavGraph
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeRepository.migrateFromSharedPreferences()

        var appTheme by mutableStateOf(AppTheme.Fantasy)

        lifecycleScope.launch {
            themeRepository.appThemeFlow.collect { appTheme = it }
        }

        setContent {
            MediaTrackerTheme(appTheme = appTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        onThemeChange = { theme ->
                            appTheme = theme
                            lifecycleScope.launch {
                                themeRepository.setAppTheme(theme)
                            }
                        },
                    )
                }
            }
        }
    }
}
