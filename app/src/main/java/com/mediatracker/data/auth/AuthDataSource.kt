package com.mediatracker.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class AuthResult(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userEmail: String? = null,
    val userName: String? = null,
)

@Singleton
class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth?,
) {

    val isAvailable: Boolean
        get() = auth != null

    val isLoggedIn: Boolean
        get() = auth?.currentUser != null

    fun getUserEmail(): String? = auth?.currentUser?.email

    fun getUserName(): String? = auth?.currentUser?.displayName

    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        val firebaseAuth = auth ?: return AuthResult(error = "Firebase no está configurado. Añade google-services.json.")
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            AuthResult(
                isLoggedIn = true,
                userEmail = user?.email,
                userName = user?.displayName,
            )
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            AuthResult(error = mapAuthError(e))
        }
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult {
        val firebaseAuth = auth ?: return AuthResult(error = "Firebase no está configurado. Añade google-services.json.")
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
            val user = firebaseAuth.currentUser
            AuthResult(
                isLoggedIn = true,
                userEmail = user?.email,
                userName = user?.displayName,
            )
        } catch (e: Exception) {
            Timber.e(e, "Register failed")
            AuthResult(error = mapAuthError(e))
        }
    }

    fun logout() {
        auth?.signOut()
    }

    internal fun mapAuthError(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> when (e.errorCode) {
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil. Usa al menos 6 caracteres."
            else -> "La contraseña no es lo suficientemente segura."
        }
        is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del email no es válido."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este email."
            "ERROR_INVALID_CREDENTIAL" -> "Las credenciales son inválidas."
            else -> "Credenciales incorrectas. Revisa tu email y contraseña."
        }
        is FirebaseAuthUserCollisionException -> when (e.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este email."
            else -> "Este email ya está registrado."
        }
        is FirebaseAuthEmailException -> "No se pudo verificar el email. Revisa tu bandeja de entrada."
        else -> when {
            e.message?.contains("network", ignoreCase = true) == true ->
                "Sin conexión a internet. Revisa tu red."
            e.message?.contains("blocked", ignoreCase = true) == true ->
                "Demasiados intentos. Inténtalo más tarde."
            else -> "Ha ocurrido un error inesperado. Inténtalo de nuevo."
        }
    }
}
