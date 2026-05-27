package com.mediatracker.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.LocalAppTheme
import com.mediatracker.presentation.theme.fanAppColors

private val AVATARS = listOf("🦊", "🐼", "🐸", "🦁", "🐯", "🐺", "🦋", "🌟", "🎭", "🦄", "🐉", "🎪")

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToTheme: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToFanCard: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val fanColors = MaterialTheme.fanAppColors
    val appTheme = LocalAppTheme.current
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val avatarId by viewModel.avatarId.collectAsStateWithLifecycle()
    val showEditNameDialog by viewModel.showEditNameDialog.collectAsStateWithLifecycle()
    val showAvatarDialog by viewModel.showAvatarDialog.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val displayName = userName?.takeIf { it.isNotBlank() }?.capitalizeWords() ?: "Usuario"
    val initial = displayName.first().uppercaseChar().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.statusBarsPadding())

        // ── Profile Banner ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(fanColors.gradient2))
                .padding(18.dp),
        ) {
            // Decorative sparkles
            Text("✨", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            Text("✨", fontSize = 10.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .clickable { viewModel.openAvatarDialog() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarId != null) {
                        Text(text = avatarId!!, fontSize = 34.sp)
                    } else {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = DisplayFontFamily,
                                letterSpacing = (-0.3).sp,
                            ),
                            color = Color.White,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        IconButton(
                            onClick = { viewModel.openEditNameDialog() },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.profile_edit_name),
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Text(
                        text = userEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f),
                        fontWeight = FontWeight.Medium,
                    )
                    // Level badge
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = stats.levelIcon, fontSize = 12.sp)
                            Text(
                                text = "Nv.${stats.level} · ${stats.levelTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // ── Stats glass tiles ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val statItems = listOf(
                Triple("🎬", stringResource(R.string.profile_series), stats.seriesInProgress + stats.seriesCompleted),
                Triple("🎥", stringResource(R.string.profile_movies), stats.moviesInProgress + stats.moviesCompleted),
                Triple("📖", stringResource(R.string.profile_books), stats.booksInProgress + stats.booksCompleted),
            )
            statItems.forEach { (icon, label, count) ->
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    radius = 18.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = DisplayFontFamily,
                                letterSpacing = (-0.4).sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
        }
    }

    if (streak.currentStreak > 0 || streak.longestStreak > 0) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlassSurface(modifier = Modifier.weight(1f), radius = 18.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🔥", fontSize = 24.sp)
                    Text(
                        text = streak.currentStreak.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = DisplayFontFamily,
                            letterSpacing = (-0.4).sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.streak_current),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            GlassSurface(modifier = Modifier.weight(1f), radius = 18.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("⚡", fontSize = 24.sp)
                    Text(
                        text = streak.longestStreak.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = DisplayFontFamily,
                            letterSpacing = (-0.4).sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.streak_longest),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))

        // ── Settings section ──────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.profile_settings),
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.3).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(8.dp))

        SettingRow(
            icon = "🃏",
            label = stringResource(R.string.fancard_title),
            highlight = true,
            onClick = onNavigateToFanCard,
        )

        SettingRow(
            icon = "🏆",
            label = stringResource(R.string.achievements_title),
            highlight = true,
            onClick = onNavigateToAchievements,
        )

        SettingRow(
            icon = "🎨",
            label = stringResource(R.string.profile_theme),
            value = appTheme.name,
            highlight = true,
            onClick = onNavigateToTheme,
        )
        SettingRow(icon = "🌐", label = stringResource(R.string.profile_language), value = stringResource(R.string.profile_language_value))
        SettingRow(
            icon = "🔔",
            label = stringResource(R.string.profile_notifications),
            value = if (unreadCount > 0) "$unreadCount" else stringResource(R.string.profile_notif_enabled),
            onClick = onNavigateToNotifications,
        )
        SettingRow(icon = "🔒", label = stringResource(R.string.profile_privacy), onClick = onNavigateToPrivacy)
        SettingRow(icon = "🔑", label = stringResource(R.string.change_password), onClick = onNavigateToChangePassword)

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("↩  ${stringResource(R.string.profile_logout)}", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(32.dp))
    }

    // ── Edit Name Dialog ──────────────────────────────────────────────────────
    if (showEditNameDialog) {
        var nameText by rememberSaveable { mutableStateOf(userName ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.closeEditNameDialog() },
            title = { Text(stringResource(R.string.profile_edit_name)) },
            text = {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text(stringResource(R.string.profile_edit_name_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.updateUserName(nameText) },
                    enabled = nameText.isNotBlank(),
                ) { Text(stringResource(R.string.profile_save)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEditNameDialog() }) { Text(stringResource(R.string.profile_cancel)) }
            },
        )
    }

    // ── Avatar Picker Dialog ──────────────────────────────────────────────────
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAvatarDialog() },
            title = { Text(stringResource(R.string.profile_choose_avatar)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(AVATARS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (emoji == avatarId) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .clickable { viewModel.updateAvatar(emoji) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emoji, fontSize = 28.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.closeAvatarDialog() }) { Text(stringResource(R.string.profile_cancel)) }
            },
        )
    }
}

// ─── Settings row ─────────────────────────────────────────────────────────────
@Composable
private fun SettingRow(
    icon: String,
    label: String,
    value: String = "",
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val fanColors = MaterialTheme.fanAppColors
    val bgColor = if (highlight) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = if (highlight) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
        if (onClick != null) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
