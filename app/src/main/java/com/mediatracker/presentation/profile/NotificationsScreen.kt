package com.mediatracker.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassBackHeader
import androidx.compose.ui.tooling.preview.Preview
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.statusBarsPadding())
        GlassBackHeader(
            title = stringResource(R.string.notifications_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.notifications_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            NotificationSwitchRow(
                emoji = "🔔",
                title = stringResource(R.string.notifications_push),
                subtitle = stringResource(R.string.notifications_push_subtitle),
                checked = prefs.pushEnabled,
                onCheckedChange = { viewModel.onPushEnabledChanged(it) },
            )

            Spacer(Modifier.height(12.dp))

            NotificationSwitchRow(
                emoji = "🆕",
                title = stringResource(R.string.notifications_new_releases),
                subtitle = stringResource(R.string.notifications_new_releases_subtitle),
                checked = prefs.newReleasesEnabled,
                onCheckedChange = { viewModel.onNewReleasesChanged(it) },
                enabled = prefs.pushEnabled,
            )

            Spacer(Modifier.height(12.dp))

            NotificationSwitchRow(
                emoji = "💡",
                title = stringResource(R.string.notifications_recommendations),
                subtitle = stringResource(R.string.notifications_recommendations_subtitle),
                checked = prefs.recommendationsEnabled,
                onCheckedChange = { viewModel.onRecommendationsChanged(it) },
                enabled = prefs.pushEnabled,
            )

            Spacer(Modifier.height(12.dp))

            NotificationSwitchRow(
                emoji = "📊",
                title = stringResource(R.string.notifications_weekly_summary),
                subtitle = stringResource(R.string.notifications_weekly_summary_subtitle),
                checked = prefs.weeklySummaryEnabled,
                onCheckedChange = { viewModel.onWeeklySummaryChanged(it) },
                enabled = prefs.pushEnabled,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NotificationSwitchRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(emoji, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            )
        }
        Switch(
            checked = if (enabled) checked else false,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun NotificationsScreenPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        NotificationsScreen(onBack = {})
    }
}
