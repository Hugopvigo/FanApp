package com.mediatracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.domain.usecase.GetUserStatsUseCase
import com.mediatracker.domain.usecase.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getUserStatsUseCase: GetUserStatsUseCase,
    authDataSource: AuthDataSource,
) : ViewModel() {

    val stats: StateFlow<UserStats> = getUserStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    val userName: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserName())

    val userEmail: StateFlow<String?> = authDataSource.authStateFlow()
        .map { it.userEmail }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authDataSource.getUserEmail())
}
