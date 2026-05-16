package com.mediatracker.presentation.auth

import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.data.auth.AuthResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authDataSource: AuthDataSource
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authDataSource = mockk(relaxed = true)
        every { authDataSource.isLoggedIn } returns false
        every { authDataSource.getUserEmail() } returns null
        every { authDataSource.getUserName() } returns null
        viewModel = AuthViewModel(authDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not logged in`() {
        assertFalse(viewModel.state.value.isLoggedIn)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login success updates state to logged in`() = runTest {
        coEvery { authDataSource.loginWithEmail("test@test.com", "password123") } returns
            AuthResult(isLoggedIn = true, userEmail = "test@test.com", userName = "Test")

        viewModel.login("test@test.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLoggedIn)
        assertEquals("test@test.com", viewModel.state.value.userEmail)
        assertEquals("Test", viewModel.state.value.userName)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login failure sets error in state`() = runTest {
        coEvery { authDataSource.loginWithEmail("test@test.com", "wrong") } returns
            AuthResult(error = "Credenciales incorrectas. Revisa tu email y contraseña.")

        viewModel.login("test@test.com", "wrong")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoggedIn)
        assertEquals("Credenciales incorrectas. Revisa tu email y contraseña.", viewModel.state.value.error)
    }

    @Test
    fun `register success updates state to logged in`() = runTest {
        coEvery { authDataSource.registerWithEmail("Test", "test@test.com", "password123") } returns
            AuthResult(isLoggedIn = true, userEmail = "test@test.com", userName = "Test")

        viewModel.register("Test", "test@test.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLoggedIn)
        assertEquals("test@test.com", viewModel.state.value.userEmail)
    }

    @Test
    fun `register failure sets error in state`() = runTest {
        coEvery { authDataSource.registerWithEmail("Test", "test@test.com", "weak") } returns
            AuthResult(error = "La contraseña es demasiado débil. Usa al menos 6 caracteres.")

        viewModel.register("Test", "test@test.com", "weak")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoggedIn)
        assertEquals("La contraseña es demasiado débil. Usa al menos 6 caracteres.", viewModel.state.value.error)
    }

    @Test
    fun `logout calls dataSource logout`() = runTest {
        every { authDataSource.isLoggedIn } returns true
        every { authDataSource.getUserEmail() } returns "test@test.com"

        viewModel.logout()
        advanceUntilIdle()

        coVerify { authDataSource.logout() }
    }

    @Test
    fun `clearError clears error state`() = runTest {
        coEvery { authDataSource.loginWithEmail("test@test.com", "wrong") } returns
            AuthResult(error = "Error de prueba")

        viewModel.login("test@test.com", "wrong")
        advanceUntilIdle()

        assertEquals("Error de prueba", viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login sets loading state during operation`() = runTest {
        coEvery { authDataSource.loginWithEmail("test@test.com", "password123") } returns
            AuthResult(isLoggedIn = true, userEmail = "test@test.com")

        viewModel.login("test@test.com", "password123")

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }
}
