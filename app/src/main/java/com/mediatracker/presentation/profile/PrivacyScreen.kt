package com.mediatracker.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassBackHeader
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    viewModel: PrivacyViewModel = hiltViewModel(),
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.statusBarsPadding())
        GlassBackHeader(
            title = stringResource(R.string.privacy_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.privacy_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            PrivacySection(
                emoji = "🔐",
                title = stringResource(R.string.privacy_data_title),
                subtitle = stringResource(R.string.privacy_data_subtitle),
            )

            Spacer(Modifier.height(12.dp))

            PrivacySection(
                emoji = "☁️",
                title = stringResource(R.string.privacy_cloud_title),
                subtitle = stringResource(R.string.privacy_cloud_subtitle),
            )

            Spacer(Modifier.height(12.dp))

            PrivacySection(
                emoji = "📱",
                title = stringResource(R.string.privacy_local_title),
                subtitle = stringResource(R.string.privacy_local_subtitle),
            )

            Spacer(Modifier.height(12.dp))

            PrivacySection(
                emoji = "🤖",
                title = stringResource(R.string.privacy_third_party_title),
                subtitle = stringResource(R.string.privacy_third_party_subtitle),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.privacy_actions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(12.dp))

            PrivacyToggleRow(
                emoji = "👤",
                title = stringResource(R.string.privacy_toggle_public_profile),
                subtitle = stringResource(R.string.privacy_toggle_public_profile_subtitle),
                checked = settings.publicProfile,
                onCheckedChange = { viewModel.onPublicProfileChanged(it) },
            )

            Spacer(Modifier.height(8.dp))

            PrivacyToggleRow(
                emoji = "📊",
                title = stringResource(R.string.privacy_toggle_show_stats),
                subtitle = stringResource(R.string.privacy_toggle_show_stats_subtitle),
                checked = settings.showStats,
                onCheckedChange = { viewModel.onShowStatsChanged(it) },
            )

            Spacer(Modifier.height(8.dp))

            PrivacyToggleRow(
                emoji = "📚",
                title = stringResource(R.string.privacy_toggle_show_library),
                subtitle = stringResource(R.string.privacy_toggle_show_library_subtitle),
                checked = settings.showLibrary,
                onCheckedChange = { viewModel.onShowLibraryChanged(it) },
            )

            Spacer(Modifier.height(8.dp))

            PrivacyToggleRow(
                emoji = "🔄",
                title = stringResource(R.string.privacy_toggle_share_activity),
                subtitle = stringResource(R.string.privacy_toggle_share_activity_subtitle),
                checked = settings.shareActivity,
                onCheckedChange = { viewModel.onShareActivityChanged(it) },
            )

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.profile_delete_account), fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.privacy_delete_dialog_title)) },
            text = { Text(stringResource(R.string.privacy_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.privacy_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            },
        )
    }
}

@Composable
private fun PrivacyToggleRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun PrivacySection(
    emoji: String,
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun PrivacyScreenPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        PrivacyScreen(onBack = {})
    }
}
