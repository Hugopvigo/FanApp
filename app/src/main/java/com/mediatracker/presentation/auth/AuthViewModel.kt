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
    val passwordResetSent: Boolean = false,
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

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authDataSource.signInWithGoogle(idToken)
            _state.update { it.copy(isLoading = false, error = result.error) }
        }
    }

    fun login() {
        val s = _state.value
        val emailError = validateEmail(s.email)
        val passwordError = validatePassword(s.password, isRegister = false)
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authDataSource.loginWithEmail(s.email, s.password)
            _state.update { it.copy(isLoading = false, error = result.error) }
        }
    }

    fun register() {
        val s = _state.value
        val emailError = validateEmail(s.email)
        val passwordError = validatePassword(s.password, isRegister = true)
        val nameError = validateName(s.name)
        if (emailError != null || passwordError != null || nameError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError, nameError = nameError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = authDataSource.registerWithEmail(s.name, s.email, s.password)
            _state.update { it.copy(isLoading = false, error = result.error) }
        }
    }

    fun logout() {
        authDataSource.logout()
    }

    fun sendPasswordReset() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Introduce tu email primero") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authDataSource.sendPasswordReset(email)
                .onSuccess { _state.update { it.copy(isLoading = false, passwordResetSent = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearPasswordResetSent() {
        _state.update { it.copy(passwordResetSent = false) }
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
