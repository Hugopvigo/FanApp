package com.mediatracker

import android.content.Intent
import android.net.Uri
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
import com.mediatracker.data.local.LocaleRepository
import com.mediatracker.data.local.OnboardingRepository
import com.mediatracker.data.local.ThemeRepository
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.navigation.AppNavGraph
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepository: ThemeRepository
    @Inject lateinit var localeRepository: LocaleRepository
    @Inject lateinit var onboardingRepository: OnboardingRepository

    private var pendingDetail by mutableStateOf<DetailDeepLink?>(null)

    data class DetailDeepLink(val apiId: String, val mediaType: MediaType)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeRepository.migrateFromSharedPreferences()
        lifecycleScope.launch { localeRepository.applyStoredLocales() }
        parseDetailDeepLink(intent)?.let { pendingDetail = it }

        var appTheme by mutableStateOf(AppTheme.Fantasy)
        var useSystemTheme by mutableStateOf(false)
        var onboardingNeeded by mutableStateOf(false)

        lifecycleScope.launch {
            onboardingNeeded = !onboardingRepository.isOnboardingCompleted()
            combine(
                themeRepository.appThemeFlow,
                themeRepository.useSystemThemeFlow,
            ) { theme, useSystem -> theme to useSystem }
                .collect { (theme, useSystem) ->
                    appTheme = theme
                    useSystemTheme = useSystem
                }
        }

        setContent {
            MediaTrackerTheme(appTheme = appTheme, useSystemTheme = useSystemTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        onThemeChange = { theme ->
                            appTheme = theme
                            lifecycleScope.launch {
                                themeRepository.setAppTheme(theme)
                            }
                        },
                        useSystemTheme = useSystemTheme,
                        onUseSystemThemeChange = { enabled ->
                            useSystemTheme = enabled
                            lifecycleScope.launch {
                                themeRepository.setUseSystemTheme(enabled)
                            }
                        },
                        pendingDetailDeepLink = pendingDetail,
                        onDetailDeepLinkConsumed = { pendingDetail = null },
                        onboardingNeeded = onboardingNeeded,
                        onOnboardingComplete = {
                            lifecycleScope.launch {
                                onboardingRepository.markOnboardingCompleted()
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseDetailDeepLink(intent)?.let { pendingDetail = it }
    }

    private fun parseDetailDeepLink(intent: Intent?): DetailDeepLink? {
        val uri = intent?.data ?: return null
        if (uri.scheme != "mediatracker" || uri.host != "detail") return null
        val segments = uri.pathSegments
        if (segments.size < 2) return null
        val mediaType = runCatching { MediaType.valueOf(segments[0].uppercase(java.util.Locale.US)) }.getOrNull() ?: return null
        val apiId = Uri.decode(segments[1])
        if (apiId.isBlank()) return null
        return DetailDeepLink(apiId = apiId, mediaType = mediaType)
    }

}
