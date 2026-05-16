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
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isRegisterMode: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authDataSource: AuthDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authDataSource.authStateFlow().collect { result ->
                _state.update {
                    it.copy(
                        isLoggedIn = result.isLoggedIn,
                        userEmail = result.userEmail,
                        userName = result.userName,
                    )
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun updatePassword(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name, nameError = null, error = null) }
    }

    fun toggleMode() {
        _state.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }
    }

    fun login() {
        val state = _state.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password, isRegister = false)
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authDataSource.loginWithEmail(state.email, state.password)
        }
    }

    fun register() {
        val state = _state.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password, isRegister = true)
        val nameError = validateName(state.name)
        if (emailError != null || passwordError != null || nameError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError, nameError = nameError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authDataSource.registerWithEmail(state.name, state.email, state.password)
        }
    }

    fun logout() {
        authDataSource.logout()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El email es obligatorio"
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return if (!emailRegex.matches(email.trim())) "Formato de email inválido" else null
    }

    private fun validatePassword(password: String, isRegister: Boolean): String? {
        if (password.isBlank()) return "La contraseña es obligatoria"
        if (password.length < 6) return "Mínimo 6 caracteres"
        if (isRegister) {
            if (!password.any { it.isUpperCase() }) return "Debe contener al menos una mayúscula"
            if (!password.any { it.isDigit() }) return "Debe contener al menos un número"
        }
        return null
    }

    private fun validateName(name: String): String? {
        if (name.isBlank()) return "El nombre es obligatorio"
        return if (name.trim().length < 2) "Mínimo 2 caracteres" else null
    }
}
