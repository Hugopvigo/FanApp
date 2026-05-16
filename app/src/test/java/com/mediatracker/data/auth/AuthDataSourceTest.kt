package com.mediatracker.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthDataSourceTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataSource: AuthDataSource

    @Before
    fun setup() {
        auth = mockk(relaxed = true)
        every { auth.currentUser } returns null
        dataSource = AuthDataSource(auth)
    }

    @Test
    fun `isLoggedIn returns false when no user`() {
        every { auth.currentUser } returns null

        assertFalse(dataSource.isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns true when user exists`() {
        every { auth.currentUser } returns mockk()

        assertTrue(dataSource.isLoggedIn)
    }

    @Test
    fun `getUserEmail returns null when no user`() {
        every { auth.currentUser } returns null

        assertNull(dataSource.getUserEmail())
    }

    @Test
    fun `getUserName returns null when no user`() {
        every { auth.currentUser } returns null

        assertNull(dataSource.getUserName())
    }

    @Test
    fun `mapAuthError returns weak password message`() {
        val exception: FirebaseAuthWeakPasswordException = mockk {
            every { errorCode } returns "ERROR_WEAK_PASSWORD"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("La contraseña es demasiado débil. Usa al menos 6 caracteres.", result)
    }

    @Test
    fun `mapAuthError returns invalid email message`() {
        val exception: FirebaseAuthInvalidCredentialsException = mockk {
            every { errorCode } returns "ERROR_INVALID_EMAIL"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("El formato del email no es válido.", result)
    }

    @Test
    fun `mapAuthError returns wrong password message`() {
        val exception: FirebaseAuthInvalidCredentialsException = mockk {
            every { errorCode } returns "ERROR_WRONG_PASSWORD"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("La contraseña es incorrecta.", result)
    }

    @Test
    fun `mapAuthError returns user not found message`() {
        val exception: FirebaseAuthInvalidCredentialsException = mockk {
            every { errorCode } returns "ERROR_USER_NOT_FOUND"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("No existe una cuenta con este email.", result)
    }

    @Test
    fun `mapAuthError returns invalid credentials fallback message`() {
        val exception: FirebaseAuthInvalidCredentialsException = mockk {
            every { errorCode } returns "ERROR_OTHER"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("Credenciales incorrectas. Revisa tu email y contraseña.", result)
    }

    @Test
    fun `mapAuthError returns email already in use`() {
        val exception: FirebaseAuthUserCollisionException = mockk {
            every { errorCode } returns "ERROR_EMAIL_ALREADY_IN_USE"
        }

        val result = dataSource.mapAuthError(exception)

        assertEquals("Ya existe una cuenta con este email.", result)
    }

    @Test
    fun `mapAuthError returns generic error for unknown exception`() {
        val exception = RuntimeException("Something went wrong")

        val result = dataSource.mapAuthError(exception)

        assertEquals("Ha ocurrido un error inesperado. Inténtalo de nuevo.", result)
    }

    @Test
    fun `mapAuthError returns network error message for network-related exceptions`() {
        val exception = RuntimeException("Unable to resolve host: network error")

        val result = dataSource.mapAuthError(exception)

        assertEquals("Sin conexión a internet. Revisa tu red.", result)
    }

    @Test
    fun `mapAuthError returns blocked message for rate-limited exceptions`() {
        val exception = RuntimeException("Access blocked due to too many attempts")

        val result = dataSource.mapAuthError(exception)

        assertEquals("Demasiados intentos. Inténtalo más tarde.", result)
    }
}
