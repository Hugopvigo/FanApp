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
class UpdateWatchedEpisodesUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private lateinit var useCase: UpdateWatchedEpisodesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = UpdateWatchedEpisodesUseCase(userRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        val itemId = "ui_1"
        val watchedEpisodes = mapOf(1 to listOf(1, 2, 3), 2 to listOf(1))
        coEvery { userRepository.updateWatchedEpisodes(itemId, watchedEpisodes) } returns Result.success(Unit)

        useCase(itemId, watchedEpisodes)

        coVerify { userRepository.updateWatchedEpisodes(itemId, watchedEpisodes) }
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        coEvery { userRepository.updateWatchedEpisodes(any(), any()) } returns Result.success(Unit)

        val result = useCase("ui_1", mapOf(1 to listOf(1)))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { userRepository.updateWatchedEpisodes(any(), any()) } returns Result.failure(Exception("DB error"))

        val result = useCase("ui_1", mapOf(1 to listOf(1)))

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with empty map clears watched episodes`() = runTest {
        coEvery { userRepository.updateWatchedEpisodes(any(), any()) } returns Result.success(Unit)

        val result = useCase("ui_1", emptyMap())

        assertTrue(result.isSuccess)
        coVerify { userRepository.updateWatchedEpisodes("ui_1", emptyMap()) }
    }
}
