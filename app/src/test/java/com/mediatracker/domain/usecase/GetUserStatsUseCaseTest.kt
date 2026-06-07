package com.mediatracker.domain.usecase

import com.mediatracker.data.local.StreakDao
import com.mediatracker.data.local.StreakEntity
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetUserStatsUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private val streakDao = mockk<StreakDao>()
    private lateinit var useCase: GetUserStatsUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = GetUserStatsUseCase(userRepository, streakDao)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun testUserItem(
        mediaType: MediaType = MediaType.SERIES,
        status: ItemStatus = ItemStatus.COMPLETED,
        favorite: Boolean = false,
    ) = UserItem(
        id = "ui_1",
        mediaType = mediaType,
        apiId = "api_1",
        title = "Test",
        posterUrl = null,
        status = status,
        favorite = favorite,
        addedAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `invoke returns correct stats for empty library`() = runTest {
        every { userRepository.getUserItemsFlow() } returns flowOf(emptyList())
        coEvery { streakDao.get() } returns null

        val stats = useCase().first()

        assertEquals(0, stats.seriesCompleted)
        assertEquals(0, stats.moviesCompleted)
        assertEquals(0, stats.booksCompleted)
        assertEquals(1, stats.level)
    }

    @Test
    fun `invoke counts completed items by type`() = runTest {
        val items = listOf(
            testUserItem(MediaType.SERIES, ItemStatus.COMPLETED),
            testUserItem(MediaType.SERIES, ItemStatus.COMPLETED),
            testUserItem(MediaType.MOVIE, ItemStatus.COMPLETED),
            testUserItem(MediaType.BOOK, ItemStatus.IN_PROGRESS),
        )
        every { userRepository.getUserItemsFlow() } returns flowOf(items)
        coEvery { streakDao.get() } returns null

        val stats = useCase().first()

        assertEquals(2, stats.seriesCompleted)
        assertEquals(1, stats.moviesCompleted)
        assertEquals(0, stats.booksCompleted)
    }

    @Test
    fun `invoke excludes abandoned items from active count`() = runTest {
        val items = listOf(
            testUserItem(MediaType.SERIES, ItemStatus.COMPLETED),
            testUserItem(MediaType.MOVIE, ItemStatus.ABANDONED),
        )
        every { userRepository.getUserItemsFlow() } returns flowOf(items)
        coEvery { streakDao.get() } returns null

        val stats = useCase().first()

        assertEquals(1, stats.seriesCompleted)
        assertEquals(0, stats.moviesCompleted)
    }

    @Test
    fun `invoke calculates XP correctly`() = runTest {
        val items = listOf(
            testUserItem(status = ItemStatus.COMPLETED, favorite = true),
            testUserItem(status = ItemStatus.IN_PROGRESS),
            testUserItem(status = ItemStatus.WATCHLIST),
        )
        every { userRepository.getUserItemsFlow() } returns flowOf(items)
        coEvery { streakDao.get() } returns null

        val stats = useCase().first()

        // 1 completed * 25 + 1 in_progress * 10 + 3 items * 5 + 1 favorite * 5 = 55
        assertEquals(55, stats.totalXp)
    }

    @Test
    fun `invoke adds streak bonus XP`() = runTest {
        val items = listOf(
            testUserItem(status = ItemStatus.COMPLETED),
        )
        every { userRepository.getUserItemsFlow() } returns flowOf(items)
        coEvery { streakDao.get() } returns StreakEntity(bonusXp = 100)

        val stats = useCase().first()

        // 1 completed * 25 + 1 item * 5 = 30 base + 100 streak bonus = 130
        assertEquals(130, stats.totalXp)
    }

    @Test
    fun `invoke calculates level based on completed count`() = runTest {
        val items = (1..6).map {
            testUserItem(status = ItemStatus.COMPLETED)
        }
        every { userRepository.getUserItemsFlow() } returns flowOf(items)
        coEvery { streakDao.get() } returns null

        val stats = useCase().first()

        // 6 completed >= 5 → level 2 (Aficionado)
        assertEquals(2, stats.level)
        assertEquals("Aficionado", stats.levelTitle)
    }
}
