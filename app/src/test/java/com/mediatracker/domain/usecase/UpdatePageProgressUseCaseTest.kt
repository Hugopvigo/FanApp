package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdatePageProgressUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private lateinit var useCase: UpdatePageProgressUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = UpdatePageProgressUseCase(userRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        coEvery { userRepository.updatePageProgress(any(), any(), any()) } returns Result.success(Unit)

        useCase("ui_1", 100, 500)

        coVerify { userRepository.updatePageProgress("ui_1", 100, 500) }
    }

    @Test
    fun `invoke returns success`() = runTest {
        coEvery { userRepository.updatePageProgress(any(), any(), any()) } returns Result.success(Unit)

        val result = useCase("ui_1", 50, 200)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke with null currentPage`() = runTest {
        coEvery { userRepository.updatePageProgress(any(), any(), any()) } returns Result.success(Unit)

        val result = useCase("ui_1", null, 300)

        assertTrue(result.isSuccess)
        coVerify { userRepository.updatePageProgress("ui_1", null, 300) }
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        coEvery { userRepository.updatePageProgress(any(), any(), any()) } returns Result.failure(Exception("Network error"))

        val result = useCase("ui_1", 10, 100)

        assertTrue(result.isFailure)
    }
}
