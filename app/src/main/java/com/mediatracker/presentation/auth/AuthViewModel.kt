package com.mediatracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userName: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authDataSource: AuthDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                isLoggedIn = authDataSource.isLoggedIn,
                userEmail = authDataSource.getUserEmail(),
                userName = authDataSource.getUserName(),
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authDataSource.loginWithEmail(email, password)
            _state.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = result.isLoggedIn,
                    error = result.error,
                    userEmail = result.userEmail,
                    userName = result.userName,
                )
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authDataSource.registerWithEmail(name, email, password)
            _state.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = result.isLoggedIn,
                    error = result.error,
                    userEmail = result.userEmail,
                    userName = result.userName,
                )
            }
        }
    }

    fun logout() {
        authDataSource.logout()
        _state.update { AuthUiState(isLoggedIn = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
