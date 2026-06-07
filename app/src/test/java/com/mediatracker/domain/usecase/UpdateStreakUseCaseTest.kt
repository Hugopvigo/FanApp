package com.mediatracker.domain.usecase

import android.app.NotificationManager
import android.content.Context
import com.mediatracker.data.analytics.AnalyticsHelper
import com.mediatracker.data.local.NotificationDao
import com.mediatracker.data.local.StreakDao
import com.mediatracker.data.local.StreakEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateStreakUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)
    private val streakDao = mockk<StreakDao>(relaxed = true)
    private val notificationDao = mockk<NotificationDao>(relaxed = true)
    private val analytics = mockk<AnalyticsHelper>(relaxed = true)
    private lateinit var useCase: UpdateStreakUseCase

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        useCase = UpdateStreakUseCase(context, streakDao, notificationDao, analytics)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke starts streak at 1 when no previous streak`() = runTest {
        coEvery { streakDao.get() } returns null
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
    }

    @Test
    fun `invoke increments streak when consecutive day`() = runTest {
        val yesterday = LocalDate.now().minusDays(1).format(formatter)
        coEvery { streakDao.get() } returns StreakEntity(
            currentStreak = 5,
            longestStreak = 5,
            lastActiveDate = yesterday,
        )
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(6, result.currentStreak)
        assertEquals(6, result.longestStreak)
    }

    @Test
    fun `invoke resets streak when gap of more than 1 day`() = runTest {
        val threeDaysAgo = LocalDate.now().minusDays(3).format(formatter)
        coEvery { streakDao.get() } returns StreakEntity(
            currentStreak = 10,
            longestStreak = 10,
            lastActiveDate = threeDaysAgo,
        )
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(1, result.currentStreak)
        assertEquals(10, result.longestStreak)
    }

    @Test
    fun `invoke keeps streak same when called twice same day`() = runTest {
        val today = LocalDate.now().format(formatter)
        coEvery { streakDao.get() } returns StreakEntity(
            currentStreak = 3,
            longestStreak = 3,
            lastActiveDate = today,
        )
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(3, result.currentStreak)
    }

    @Test
    fun `invoke triggers 7-day milestone`() = runTest {
        val yesterday = LocalDate.now().minusDays(1).format(formatter)
        coEvery { streakDao.get() } returns StreakEntity(
            currentStreak = 6,
            longestStreak = 6,
            lastActiveDate = yesterday,
            milestonesHit = "",
        )
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(7, result.currentStreak)
        assertTrue(result.bonusXp > 0)
        coVerify { notificationDao.insert(any()) }
        coVerify { analytics.logStreakMilestone(7) }
    }

    @Test
    fun `invoke does not re-trigger already hit milestone`() = runTest {
        val yesterday = LocalDate.now().minusDays(1).format(formatter)
        coEvery { streakDao.get() } returns StreakEntity(
            currentStreak = 6,
            longestStreak = 6,
            lastActiveDate = yesterday,
            milestonesHit = "7",
        )
        coEvery { streakDao.upsert(any()) } returns Unit

        val result = useCase()

        assertEquals(7, result.currentStreak)
        assertEquals(0, result.bonusXp)
    }
}
