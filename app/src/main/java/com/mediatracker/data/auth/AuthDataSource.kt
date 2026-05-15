package com.mediatracker.data.auth

import com.google.firebase.auth.FirebaseAuth
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
    private val auth: FirebaseAuth,
) {

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    fun getUserEmail(): String? = auth.currentUser?.email

    fun getUserName(): String? = auth.currentUser?.displayName

    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            AuthResult(
                isLoggedIn = true,
                userEmail = user?.email,
                userName = user?.displayName,
            )
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            AuthResult(error = e.message)
        }
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
            val user = auth.currentUser
            AuthResult(
                isLoggedIn = true,
                userEmail = user?.email,
                userName = user?.displayName,
            )
        } catch (e: Exception) {
            Timber.e(e, "Register failed")
            AuthResult(error = e.message)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
