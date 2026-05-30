package com.mediatracker.presentation.auth

import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.data.auth.AuthResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
        every { authDataSource.authStateFlow() } returns flowOf(AuthResult())
        viewModel = AuthViewModel(authDataSource, mockk(relaxed = true))
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
    fun `login sets form email and password`() = runTest {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("password123")

        assertEquals("test@test.com", viewModel.state.value.email)
        assertEquals("password123", viewModel.state.value.password)
    }

    @Test
    fun `register sets form name`() = runTest {
        viewModel.updateName("Test User")
        viewModel.toggleMode()

        assertEquals("Test User", viewModel.state.value.name)
        assertTrue(viewModel.state.value.isRegisterMode)
    }

    @Test
    fun `login calls dataSource loginWithEmail`() = runTest {
        coEvery { authDataSource.loginWithEmail("test@test.com", "pass123") } returns AuthResult()

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("pass123")
        viewModel.login()
        advanceUntilIdle()

        coVerify { authDataSource.loginWithEmail("test@test.com", "pass123") }
    }

    @Test
    fun `register calls dataSource registerWithEmail`() = runTest {
        coEvery { authDataSource.registerWithEmail("Test", "test@test.com", "Password1") } returns AuthResult()

        viewModel.toggleMode()
        viewModel.updateName("Test")
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("Password1")
        viewModel.register()
        advanceUntilIdle()

        coVerify { authDataSource.registerWithEmail("Test", "test@test.com", "Password1") }
    }

    @Test
    fun `logout calls dataSource logout`() = runTest {
        every { authDataSource.logout() } just runs

        viewModel.logout()

        coVerify { authDataSource.logout() }
    }

    @Test
    fun `clearError clears error state`() {
        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `email validation rejects empty email`() {
        viewModel.updateEmail("")
        viewModel.login()

        assertEquals("El email es obligatorio", viewModel.state.value.emailError)
    }

    @Test
    fun `email validation rejects invalid format`() {
        viewModel.updateEmail("notanemail")
        viewModel.login()

        assertEquals("Formato de email inválido", viewModel.state.value.emailError)
    }

    @Test
    fun `password validation rejects short password`() {
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("abc")
        viewModel.login()

        assertEquals("Mínimo 6 caracteres", viewModel.state.value.passwordError)
    }

    @Test
    fun `name validation rejects short name in register mode`() {
        viewModel.toggleMode()
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("Password1")
        viewModel.updateName("A")
        viewModel.register()

        assertEquals("Mínimo 2 caracteres", viewModel.state.value.nameError)
    }

    @Test
    fun `register validation requires uppercase`() {
        viewModel.toggleMode()
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("abcdef1")
        viewModel.updateName("Test")
        viewModel.register()

        assertEquals("Debe contener al menos una mayúscula", viewModel.state.value.passwordError)
    }

    @Test
    fun `register validation requires digit`() {
        viewModel.toggleMode()
        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("Abcdef")
        viewModel.updateName("Test")
        viewModel.register()

        assertEquals("Debe contener al menos un número", viewModel.state.value.passwordError)
    }

    @Test
    fun `toggleMode switches between login and register`() {
        assertFalse(viewModel.state.value.isRegisterMode)

        viewModel.toggleMode()
        assertTrue(viewModel.state.value.isRegisterMode)

        viewModel.toggleMode()
        assertFalse(viewModel.state.value.isRegisterMode)
    }

    @Test
    fun `validation errors clear when field updates`() {
        viewModel.updateEmail("bad")
        viewModel.login()
        assertEquals("Formato de email inválido", viewModel.state.value.emailError)

        viewModel.updateEmail("good@test.com")
        assertNull(viewModel.state.value.emailError)
    }
}
