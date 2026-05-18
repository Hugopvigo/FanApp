package com.mediatracker.presentation.profile

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

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authDataSource: AuthDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun updateCurrentPassword(value: String) {
        _state.update { it.copy(currentPassword = value, currentPasswordError = null, error = null) }
    }

    fun updateNewPassword(value: String) {
        _state.update { it.copy(newPassword = value, newPasswordError = null, error = null) }
    }

    fun updateConfirmPassword(value: String) {
        _state.update { it.copy(confirmPassword = value, confirmPasswordError = null, error = null) }
    }

    fun changePassword() {
        val s = _state.value
        val currentError = if (s.currentPassword.isBlank()) "Introduce tu contraseña actual" else null
        val newError = validateNewPassword(s.newPassword)
        val confirmError = if (s.newPassword != s.confirmPassword) "Las contraseñas no coinciden" else null

        if (currentError != null || newError != null || confirmError != null) {
            _state.update { it.copy(currentPasswordError = currentError, newPasswordError = newError, confirmPasswordError = confirmError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authDataSource.changePassword(s.currentPassword, s.newPassword)
                .onSuccess { _state.update { it.copy(isLoading = false, success = true) } }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("wrong-password", ignoreCase = true) == true ||
                        e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                            "La contraseña actual es incorrecta"
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Sin conexión a internet"
                        else -> "Error al cambiar la contraseña. Inténtalo de nuevo."
                    }
                    _state.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private fun validateNewPassword(password: String): String? {
        if (password.isBlank()) return "La nueva contraseña es obligatoria"
        if (password.length < 6) return "Mínimo 6 caracteres"
        if (!password.any { it.isUpperCase() }) return "Debe contener al menos una mayúscula"
        if (!password.any { it.isDigit() }) return "Debe contener al menos un número"
        return null
    }
}
