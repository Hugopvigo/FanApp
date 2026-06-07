package com.mediatracker.presentation.library

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import com.mediatracker.domain.usecase.SyncUserItemsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getUserItemsUseCase = mockk<GetUserItemsUseCase>()
    private val syncUserItemsUseCase = mockk<SyncUserItemsUseCase>(relaxed = true)
    private lateinit var itemsFlow: MutableStateFlow<List<UserItem>>
    private lateinit var viewModel: LibraryViewModel

    private fun testUserItem(
        mediaType: MediaType = MediaType.SERIES,
        status: ItemStatus = ItemStatus.WATCHLIST,
    ) = UserItem(
        id = "ui_${mediaType.name}_$status",
        mediaType = mediaType,
        apiId = "api_1",
        title = "Test",
        posterUrl = null,
        status = status,
        favorite = false,
        addedAt = 0L,
        updatedAt = 0L,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemsFlow = MutableStateFlow(emptyList())
        every { getUserItemsUseCase.observeAll() } returns itemsFlow
        coEvery { syncUserItemsUseCase() } returns Result.success(Unit)
        viewModel = LibraryViewModel(getUserItemsUseCase, syncUserItemsUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is WATCHLIST with empty items`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()

        assertEquals(ItemStatus.WATCHLIST, viewModel.state.value.selectedStatus)
        assertTrue(viewModel.state.value.items.isEmpty())
        job.cancel()
    }

    @Test
    fun `onStatusSelected updates selectedStatus and filters items`() = runTest {
        val items = listOf(
            testUserItem(status = ItemStatus.WATCHLIST),
            testUserItem(status = ItemStatus.IN_PROGRESS),
            testUserItem(status = ItemStatus.COMPLETED),
        )
        itemsFlow.value = items
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.onStatusSelected(ItemStatus.IN_PROGRESS)
        advanceUntilIdle()

        assertEquals(ItemStatus.IN_PROGRESS, viewModel.state.value.selectedStatus)
        assertEquals(1, viewModel.state.value.items.size)
        assertEquals(ItemStatus.IN_PROGRESS, viewModel.state.value.items.first().status)
        job.cancel()
    }

    @Test
    fun `onMediaTypeSelected filters by media type`() = runTest {
        val items = listOf(
            testUserItem(mediaType = MediaType.SERIES, status = ItemStatus.WATCHLIST),
            testUserItem(mediaType = MediaType.MOVIE, status = ItemStatus.WATCHLIST),
            testUserItem(mediaType = MediaType.BOOK, status = ItemStatus.WATCHLIST),
        )
        itemsFlow.value = items
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.onMediaTypeSelected(MediaType.MOVIE)
        advanceUntilIdle()

        assertEquals(MediaType.MOVIE, viewModel.state.value.selectedMediaType)
        assertEquals(1, viewModel.state.value.items.size)
        assertEquals(MediaType.MOVIE, viewModel.state.value.items.first().mediaType)
        job.cancel()
    }

    @Test
    fun `init calls syncItems`() = runTest {
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()

        coVerify { syncUserItemsUseCase() }
        job.cancel()
    }

    @Test
    fun `filtering combines status and media type`() = runTest {
        val items = listOf(
            testUserItem(mediaType = MediaType.SERIES, status = ItemStatus.IN_PROGRESS),
            testUserItem(mediaType = MediaType.MOVIE, status = ItemStatus.IN_PROGRESS),
            testUserItem(mediaType = MediaType.SERIES, status = ItemStatus.WATCHLIST),
        )
        itemsFlow.value = items
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.onStatusSelected(ItemStatus.IN_PROGRESS)
        viewModel.onMediaTypeSelected(MediaType.SERIES)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals(MediaType.SERIES, viewModel.state.value.items.first().mediaType)
        job.cancel()
    }
}
