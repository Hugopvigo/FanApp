package com.mediatracker.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassBackHeader
import androidx.compose.ui.tooling.preview.Preview

private data class ThemeOption(
    val theme: AppTheme,
    val nameResId: Int,
    val blurbResId: Int,
    val isDark: Boolean,
    val bgColor: Color,
    val surfaceColor: Color,
    val primaryColor: Color,
    val accentGradient: List<Color>,
    val cardGradient: List<Color>,
)

private val themeOptions = listOf(
    ThemeOption(
        theme          = AppTheme.Fantasy,
        nameResId      = R.string.theme_name_fantasy,
        blurbResId     = R.string.theme_blurb_fantasy,
        isDark         = false,
        bgColor        = FantasyBg,
        surfaceColor   = FantasySurfaceSolid,
        primaryColor   = FantasyPrimary,
        accentGradient = listOf(Color(0xFFB794F6), Color(0xFFEC7EB1)),
        cardGradient   = listOf(Color(0xFFF9C6D9), Color(0xFFC9B1EF)),
    ),
    ThemeOption(
        theme          = AppTheme.Light,
        nameResId      = R.string.theme_name_light,
        blurbResId     = R.string.theme_blurb_light,
        isDark         = false,
        bgColor        = LightBg,
        surfaceColor   = LightSurface,
        primaryColor   = LightPrimary,
        accentGradient = listOf(Color(0xFF6D4DC7), Color(0xFFD44A8F)),
        cardGradient   = listOf(Color(0xFFF6C5DC), Color(0xFFD9CCFF)),
    ),
    ThemeOption(
        theme          = AppTheme.Purple,
        nameResId      = R.string.theme_name_purple,
        blurbResId     = R.string.theme_blurb_purple,
        isDark         = true,
        bgColor        = PurpleBg,
        surfaceColor   = PurpleSurface,
        primaryColor   = PurplePrimary,
        accentGradient = listOf(Color(0xFFB794F6), Color(0xFFEC7EB1)),
        cardGradient   = listOf(Color(0xFF4A2D7A), Color(0xFF7D3A6F)),
    ),
    ThemeOption(
        theme          = AppTheme.Dark,
        nameResId      = R.string.theme_name_dark,
        blurbResId     = R.string.theme_blurb_dark,
        isDark         = true,
        bgColor        = DarkBg,
        surfaceColor   = DarkSurface,
        primaryColor   = DarkPrimary,
        accentGradient = listOf(Color(0xFF4A6CD4), Color(0xFF6D92FF)),
        cardGradient   = listOf(Color(0xFF1A2238), Color(0xFF2A2D36)),
    ),
)

@Composable
fun ThemeScreen(
    currentTheme: AppTheme,
    useSystemTheme: Boolean = false,
    onThemeSelected: (AppTheme) -> Unit,
    onUseSystemThemeChange: (Boolean) -> Unit = {},
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.statusBarsPadding())
        GlassBackHeader(
            title = stringResource(R.string.profile_theme),
            onBack = onBack,
        )
        LazyColumn(
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 24.dp,
                start = 20.dp,
                end = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.theme_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }

            items(themeOptions, key = { it.theme.name }) { option ->
                ThemeCard(
                    option = option,
                    isSelected = option.theme == currentTheme,
                    onClick = { onThemeSelected(option.theme) },
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                AutoSwitchRow(
                    checked = useSystemTheme,
                    onCheckedChange = onUseSystemThemeChange,
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    option: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val accentBrush = Brush.linearGradient(option.accentGradient)
    val shape = RoundedCornerShape(20.dp)

    val borderMod = if (isSelected) {
        Modifier.border(width = 2.dp, brush = accentBrush, shape = shape)
    } else {
        Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = shape)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().then(borderMod),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniPhonePreview(option)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(option.nameResId),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                text = stringResource(R.string.theme_active),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(option.blurbResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (option.accentGradient + option.primaryColor).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(color)
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(999.dp),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPhonePreview(option: ThemeOption) {
    val cardBrush = Brush.linearGradient(option.cardGradient)
    val accentBrush = Brush.linearGradient(option.accentGradient)
    val textColor = if (option.isDark) Color(0xFFECE6F5) else Color(0xFF3A1F4F)

    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(option.bgColor),
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = "Aa",
                fontSize = 8.sp,
                fontFamily = DisplayFontFamily,
                color = textColor,
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cardBrush),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentBrush),
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(option.primaryColor),
            )
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(option.primaryColor.copy(alpha = 0.3f)),
            )
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(option.primaryColor.copy(alpha = 0.3f)),
            )
        }
    }
}

@Composable
private fun AutoSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🌙", fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.theme_auto),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.theme_auto_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun ThemeScreenPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        ThemeScreen(
            currentTheme = AppTheme.Fantasy,
            onThemeSelected = {},
            onBack = {},
        )
    }
}
