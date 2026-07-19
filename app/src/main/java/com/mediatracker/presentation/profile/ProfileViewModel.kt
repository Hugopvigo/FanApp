package com.mediatracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.data.local.LocaleRepository
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.domain.model.UserStats
import com.mediatracker.domain.model.UserStreak
import com.mediatracker.domain.usecase.CheckAchievementsUseCase
import com.mediatracker.domain.usecase.GetUserStatsUseCase
import com.mediatracker.domain.usecase.UpdateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserStatsUseCase: GetUserStatsUseCase,
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    notificationDao: NotificationDao,
    private val updateStreakUseCase: UpdateStreakUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    private val localeRepository: LocaleRepository,
) : ViewModel() {

    companion object {
        const val FOLLOW_SYSTEM = "system"
    }

    val languageCode: StateFlow<String> = localeRepository.languageCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocaleRepository.FOLLOW_SYSTEM)

    val stats: StateFlow<UserStats> = getUserStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val userName: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserName())

    val userEmail: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userEmail }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserEmail())

    val unreadNotificationCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _avatarId = MutableStateFlow<String?>(null)
    val avatarId: StateFlow<String?> = _avatarId.asStateFlow()

    private val _showEditNameDialog = MutableStateFlow(false)
    val showEditNameDialog: StateFlow<Boolean> = _showEditNameDialog.asStateFlow()

    private val _showAvatarDialog = MutableStateFlow(false)
    val showAvatarDialog: StateFlow<Boolean> = _showAvatarDialog.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _streak = MutableStateFlow(UserStreak(0, 0, ""))
    val streak: StateFlow<UserStreak> = _streak.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _emailVerificationSent = MutableStateFlow(false)
    val emailVerificationSent: StateFlow<Boolean> = _emailVerificationSent.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    init {
        loadAvatar()
        loadStreak()
        refreshEmailVerificationStatus()
    }

    fun refreshEmailVerificationStatus() {
        viewModelScope.launch {
            _isEmailVerified.value = authDataSource.refreshEmailVerified()
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            authDataSource.sendEmailVerification()
                .onSuccess { _emailVerificationSent.value = true }
        }
    }

    private fun loadStreak() {
        viewModelScope.launch {
            _streak.value = updateStreakUseCase()
            checkAchievementsUseCase()
        }
    }

    fun openLanguageDialog() { _showLanguageDialog.value = true }
    fun closeLanguageDialog() { _showLanguageDialog.value = false }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            localeRepository.setLanguageCode(code)
            _showLanguageDialog.value = false
        }
    }

    private fun loadAvatar() {
        viewModelScope.launch {
            firestoreDataSource.getAvatarId()
                .onSuccess { _avatarId.value = it }
        }
    }

    fun openEditNameDialog() { _showEditNameDialog.value = true }
    fun closeEditNameDialog() { _showEditNameDialog.value = false }
    fun openAvatarDialog() { _showAvatarDialog.value = true }
    fun closeAvatarDialog() { _showAvatarDialog.value = false }

    fun updateUserName(name: String) {
        val trimmed = name.trim().ifBlank { return }
        viewModelScope.launch {
            _isUpdating.value = true
            authDataSource.updateUserName(trimmed)
            _isUpdating.value = false
            _showEditNameDialog.value = false
        }
    }

    fun updateAvatar(avatarId: String) {
        viewModelScope.launch {
            _avatarId.value = avatarId
            _showAvatarDialog.value = false
            firestoreDataSource.updateAvatarId(avatarId)
        }
    }
}
