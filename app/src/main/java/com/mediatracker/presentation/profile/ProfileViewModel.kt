package com.mediatracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.domain.usecase.GetUserStatsUseCase
import com.mediatracker.domain.usecase.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserStatsUseCase: GetUserStatsUseCase,
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
) : ViewModel() {

    val stats: StateFlow<UserStats> = getUserStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val userName: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserName())

    val userEmail: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userEmail }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserEmail())

    private val _avatarId = MutableStateFlow<String?>(null)
    val avatarId: StateFlow<String?> = _avatarId.asStateFlow()

    private val _showEditNameDialog = MutableStateFlow(false)
    val showEditNameDialog: StateFlow<Boolean> = _showEditNameDialog.asStateFlow()

    private val _showAvatarDialog = MutableStateFlow(false)
    val showAvatarDialog: StateFlow<Boolean> = _showAvatarDialog.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    init {
        loadAvatar()
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
            _isUpdating.update { false }
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
