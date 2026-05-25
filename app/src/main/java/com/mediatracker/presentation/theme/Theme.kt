package com.mediatracker.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    Fantasy, Light, Purple, Dark;

    val isDark get() = this == Purple || this == Dark
}

data class FanAppColors(
    val statusWant: Color,
    val statusProgress: Color,
    val statusDone: Color,
    val statusDropped: Color,
    val statusFav: Color,
    val textDim: Color,
    val borderColor: Color,
    val borderStrong: Color,
    val surfaceAlpha: Color,
    val surfaceGlass: Color,
    val surfaceElev: Color,
    val hairlineColor: Color,
    val sparkleColor: Color,
    val primarySoftColor: Color,
    val navBarBg: Color,
    val gradient1: List<Color>,
    val gradient2: List<Color>,
    val gradientAccent: List<Color>,
    val isDark: Boolean,
)

val LocalFanAppColors = staticCompositionLocalOf {
    FanAppColors(
        statusWant       = FantasyStatusWant,
        statusProgress   = FantasyStatusProg,
        statusDone       = FantasyStatusDone,
        statusDropped    = FantasyStatusDropped,
        statusFav        = FantasyStatusFav,
        textDim          = FantasyTextDim,
        borderColor      = FantasyBorder,
        borderStrong     = FantasyBorderStrong,
        surfaceAlpha     = FantasySurface,
        surfaceGlass     = FantasySurfaceGlass,
        surfaceElev      = FantasySurfaceElev,
        hairlineColor    = FantasyHairline,
        sparkleColor     = FantasySparkle,
        primarySoftColor = FantasyPrimarySoft,
        navBarBg         = FantasyNavBar,
        gradient1        = listOf(Color(0xFFF9C6D9), Color(0xFFC9B1EF)),
        gradient2        = listOf(Color(0xFFFFD6E8), Color(0xFFD9C4FF)),
        gradientAccent   = listOf(Color(0xFFB794F6), Color(0xFFEC7EB1)),
        isDark           = false,
    )
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.Fantasy }

val MaterialTheme.fanAppColors: FanAppColors
    @Composable get() = LocalFanAppColors.current

val MaterialTheme.appTheme: AppTheme
    @Composable get() = LocalAppTheme.current

// ─── Color Schemes ────────────────────────────────────────────────────────

private val FantasyColorScheme = lightColorScheme(
    primary              = FantasyPrimary,
    onPrimary            = Color.White,
    primaryContainer     = FantasyPrimarySoft,
    onPrimaryContainer   = FantasyText,
    secondary            = FantasyAccent,
    onSecondary          = Color.White,
    secondaryContainer   = FantasyAccentSoft,
    onSecondaryContainer = FantasyText,
    tertiary             = FantasyStatusDone,
    onTertiary           = Color.White,
    background           = FantasyBg,
    onBackground         = FantasyText,
    surface              = FantasySurfaceSolid,
    onSurface            = FantasyText,
    onSurfaceVariant     = FantasyTextMuted,
    surfaceVariant       = FantasySurfVariant,
    surfaceContainer     = FantasySurfContainer,
    outline              = FantasyBorderStrong,
    outlineVariant       = FantasyBorder,
    error                = Color(0xFFBA1A1A),
    onError              = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = Color.White,
    primaryContainer     = LightPrimarySoft,
    onPrimaryContainer   = LightText,
    secondary            = LightAccent,
    onSecondary          = Color.White,
    secondaryContainer   = LightAccentSoft,
    onSecondaryContainer = LightText,
    tertiary             = LightStatusDone,
    onTertiary           = Color.White,
    background           = LightBg,
    onBackground         = LightText,
    surface              = LightSurface,
    onSurface            = LightText,
    onSurfaceVariant     = LightTextMuted,
    surfaceVariant       = LightSurfVariant,
    surfaceContainer     = LightSurfContainer,
    outline              = LightBorderStrong,
    outlineVariant       = LightBorder,
    error                = Color(0xFFBA1A1A),
    onError              = Color.White,
)

private val PurpleColorScheme = darkColorScheme(
    primary              = PurplePrimary,
    onPrimary            = PurpleBg,
    primaryContainer     = PurplePrimarySoft,
    onPrimaryContainer   = PurpleText,
    secondary            = PurpleAccent,
    onSecondary          = PurpleBg,
    secondaryContainer   = PurpleAccentSoft,
    onSecondaryContainer = PurpleText,
    tertiary             = PurpleStatusDone,
    onTertiary           = PurpleBg,
    background           = PurpleBg,
    onBackground         = PurpleText,
    surface              = PurpleSurface,
    onSurface            = PurpleText,
    onSurfaceVariant     = PurpleTextMuted,
    surfaceVariant       = PurpleSurfVariant,
    surfaceContainer     = PurpleSurfContainer,
    outline              = PurpleBorderStrong,
    outlineVariant       = PurpleBorder,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
)

private val DarkColorScheme = darkColorScheme(
    primary              = DarkPrimary,
    onPrimary            = DarkBg,
    primaryContainer     = DarkPrimarySoft,
    onPrimaryContainer   = DarkText,
    secondary            = DarkAccent,
    onSecondary          = DarkBg,
    secondaryContainer   = DarkAccentSoft,
    onSecondaryContainer = DarkText,
    tertiary             = DarkStatusDone,
    onTertiary           = DarkBg,
    background           = DarkBg,
    onBackground         = DarkText,
    surface              = DarkSurface,
    onSurface            = DarkText,
    onSurfaceVariant     = DarkTextMuted,
    surfaceVariant       = DarkSurfVariant,
    surfaceContainer     = DarkSurfContainer,
    outline              = DarkBorderStrong,
    outlineVariant       = DarkBorder,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
)

// ─── Extended FanApp Colors ───────────────────────────────────────────────

private val FantasyFanAppColors = FanAppColors(
    statusWant       = FantasyStatusWant,
    statusProgress   = FantasyStatusProg,
    statusDone       = FantasyStatusDone,
    statusDropped    = FantasyStatusDropped,
    statusFav        = FantasyStatusFav,
    textDim          = FantasyTextDim,
    borderColor      = FantasyBorder,
    borderStrong     = FantasyBorderStrong,
    surfaceAlpha     = FantasySurface,
    surfaceGlass     = FantasySurfaceGlass,
    surfaceElev      = FantasySurfaceElev,
    hairlineColor    = FantasyHairline,
    sparkleColor     = FantasySparkle,
    primarySoftColor = FantasyPrimarySoft,
    navBarBg         = FantasyNavBar,
    gradient1        = listOf(Color(0xFFF9C6D9), Color(0xFFC9B1EF)),
    gradient2        = listOf(Color(0xFFFFD6E8), Color(0xFFD9C4FF)),
    gradientAccent   = listOf(Color(0xFFB794F6), Color(0xFFEC7EB1)),
    isDark           = false,
)

private val LightFanAppColors = FanAppColors(
    statusWant       = LightStatusWant,
    statusProgress   = LightStatusProg,
    statusDone       = LightStatusDone,
    statusDropped    = LightStatusDropped,
    statusFav        = LightStatusFav,
    textDim          = LightTextDim,
    borderColor      = LightBorder,
    borderStrong     = LightBorderStrong,
    surfaceAlpha     = LightSurface,
    surfaceGlass     = LightSurfaceGlass,
    surfaceElev      = LightSurfaceElev,
    hairlineColor    = LightHairline,
    sparkleColor     = LightSparkle,
    primarySoftColor = LightPrimarySoft,
    navBarBg         = LightNavBar,
    gradient1        = listOf(Color(0xFFF6C5DC), Color(0xFFD9CCFF)),
    gradient2        = listOf(Color(0xFFFFD1E1), Color(0xFFC8B6F7)),
    gradientAccent   = listOf(Color(0xFF6D4DC7), Color(0xFFD44A8F)),
    isDark           = false,
)

private val PurpleFanAppColors = FanAppColors(
    statusWant       = PurpleStatusWant,
    statusProgress   = PurpleStatusProg,
    statusDone       = PurpleStatusDone,
    statusDropped    = PurpleStatusDropped,
    statusFav        = PurpleStatusFav,
    textDim          = PurpleTextDim,
    borderColor      = PurpleBorder,
    borderStrong     = PurpleBorderStrong,
    surfaceAlpha     = PurpleSurface,
    surfaceGlass     = PurpleSurfaceGlass,
    surfaceElev      = PurpleSurfaceElev,
    hairlineColor    = PurpleHairline,
    sparkleColor     = PurpleSparkle,
    primarySoftColor = PurplePrimarySoft,
    navBarBg         = PurpleNavBar,
    gradient1        = listOf(Color(0xFF4A2D7A), Color(0xFF7D3A6F)),
    gradient2        = listOf(Color(0xFF5D4080), Color(0xFF8D4D7D)),
    gradientAccent   = listOf(Color(0xFFB794F6), Color(0xFFEC7EB1)),
    isDark           = true,
)

private val DarkFanAppColors = FanAppColors(
    statusWant       = DarkStatusWant,
    statusProgress   = DarkStatusProg,
    statusDone       = DarkStatusDone,
    statusDropped    = DarkStatusDropped,
    statusFav        = DarkStatusFav,
    textDim          = DarkTextDim,
    borderColor      = DarkBorder,
    borderStrong     = DarkBorderStrong,
    surfaceAlpha     = DarkSurface,
    surfaceGlass     = DarkSurfaceGlass,
    surfaceElev      = DarkSurfaceElev,
    hairlineColor    = DarkHairline,
    sparkleColor     = DarkSparkle,
    primarySoftColor = DarkPrimarySoft,
    navBarBg         = DarkNavBar,
    gradient1        = listOf(Color(0xFF1A2238), Color(0xFF2A2018)),
    gradient2        = listOf(Color(0xFF1F2229), Color(0xFF2A2D36)),
    gradientAccent   = listOf(Color(0xFF4A6CD4), Color(0xFF6D92FF)),
    isDark           = true,
)

// ─── Main Theme Composable ────────────────────────────────────────────────

@Composable
fun MediaTrackerTheme(
    appTheme: AppTheme = AppTheme.Fantasy,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appTheme) {
        AppTheme.Fantasy -> FantasyColorScheme
        AppTheme.Light   -> LightColorScheme
        AppTheme.Purple  -> PurpleColorScheme
        AppTheme.Dark    -> DarkColorScheme
    }
    val fanAppColors = when (appTheme) {
        AppTheme.Fantasy -> FantasyFanAppColors
        AppTheme.Light   -> LightFanAppColors
        AppTheme.Purple  -> PurpleFanAppColors
        AppTheme.Dark    -> DarkFanAppColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !appTheme.isDark
        }
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalFanAppColors provides fanAppColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
