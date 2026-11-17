package com.mediatracker.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.presentation.profile.components.AvatarPickerDialog
import com.mediatracker.presentation.profile.components.EditNameDialog
import com.mediatracker.presentation.profile.components.EmailVerificationBanner
import com.mediatracker.presentation.profile.components.LanguageDialog
import com.mediatracker.presentation.profile.components.ProfileBanner
import com.mediatracker.presentation.profile.components.ProfileStatsTiles
import com.mediatracker.presentation.profile.components.SettingsSection
import com.mediatracker.presentation.profile.components.StreakTiles
import com.mediatracker.presentation.theme.LocalAppTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToTheme: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToFanCard: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val appTheme = LocalAppTheme.current
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val avatarId by viewModel.avatarId.collectAsStateWithLifecycle()
    val showEditNameDialog by viewModel.showEditNameDialog.collectAsStateWithLifecycle()
    val showAvatarDialog by viewModel.showAvatarDialog.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val languageCode by viewModel.languageCode.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()
    val isEmailVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()
    val emailVerificationSent by viewModel.emailVerificationSent.collectAsStateWithLifecycle()
    val languageLabel = when (languageCode) {
        ProfileViewModel.FOLLOW_SYSTEM -> stringResource(R.string.profile_language_system)
        "es" -> stringResource(R.string.profile_language_spanish)
        else -> stringResource(R.string.profile_language_english)
    }
    val displayName = userName?.takeIf { it.isNotBlank() }?.capitalizeWords() ?: stringResource(R.string.profile_default_name)
    val initial = displayName.first().uppercaseChar().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.statusBarsPadding())

        ProfileBanner(
            displayName = displayName,
            initial = initial,
            userEmail = userEmail,
            avatarId = avatarId,
            stats = stats,
            onEditName = { viewModel.openEditNameDialog() },
            onAvatarClick = { viewModel.openAvatarDialog() },
        )

        ProfileStatsTiles(stats = stats)

        StreakTiles(streak = streak)

        if (!isEmailVerified) {
            EmailVerificationBanner(
                userEmail = userEmail,
                emailVerificationSent = emailVerificationSent,
                onCheck = { viewModel.refreshEmailVerificationStatus() },
                onResend = { viewModel.sendVerificationEmail() },
            )
        }

        SettingsSection(
            appTheme = appTheme,
            languageLabel = languageLabel,
            unreadCount = unreadCount,
            onNavigateToFanCard = onNavigateToFanCard,
            onNavigateToAchievements = onNavigateToAchievements,
            onNavigateToLeaderboard = onNavigateToLeaderboard,
            onNavigateToStats = onNavigateToStats,
            onNavigateToTheme = onNavigateToTheme,
            onLanguageClick = { viewModel.openLanguageDialog() },
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToPrivacy = onNavigateToPrivacy,
            onNavigateToChangePassword = onNavigateToChangePassword,
            onNavigateToImport = onNavigateToImport,
            onLogout = onLogout,
        )
    }

    if (showEditNameDialog) {
        EditNameDialog(
            userName = userName,
            onDismiss = { viewModel.closeEditNameDialog() },
            onSave = { viewModel.updateUserName(it) },
        )
    }

    if (showAvatarDialog) {
        AvatarPickerDialog(
            avatarId = avatarId,
            onSelect = { viewModel.updateAvatar(it) },
            onDismiss = { viewModel.closeAvatarDialog() },
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            languageCode = languageCode,
            followSystemCode = ProfileViewModel.FOLLOW_SYSTEM,
            onSelect = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.closeLanguageDialog() },
        )
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
