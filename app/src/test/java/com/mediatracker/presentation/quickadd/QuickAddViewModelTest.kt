package com.mediatracker.presentation.quickadd

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import com.mediatracker.domain.usecase.AddUserItemUseCase
import com.mediatracker.domain.usecase.SearchMediaUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickAddViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchMediaUseCase: SearchMediaUseCase
    private lateinit var addUserItemUseCase: AddUserItemUseCase
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: QuickAddViewModel

    private val testMedia = MediaItem(
        id = "movie_123",
        mediaType = MediaType.MOVIE,
        title = "Inception",
        overview = "",
        posterUrl = "",
        releaseDate = "2010",
        rating = 8.8f,
        genres = emptyList(),
    )

    private val testUserItem = UserItem(
        id = "ui_1",
        mediaType = MediaType.MOVIE,
        apiId = "123",
        title = "Inception",
        posterUrl = null,
        status = ItemStatus.WATCHLIST,
        favorite = false,
        addedAt = 0L,
        updatedAt = 0L,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchMediaUseCase = mockk()
        addUserItemUseCase = mockk()
        userRepository = mockk()
        every { userRepository.getUserItemsFlow() } returns flowOf(emptyList())
        viewModel = QuickAddViewModel(searchMediaUseCase, addUserItemUseCase, userRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAddToWatchlist does not set justAddedId when operation fails`() = runTest {
        coEvery { addUserItemUseCase(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("DB error"))

        viewModel.onAddToWatchlist(testMedia)
        advanceUntilIdle()

        assertNull(viewModel.state.value.justAddedId)
        assertEquals("DB error", viewModel.state.value.error)
    }

    @Test
    fun `onAddToWatchlist sets justAddedId when operation succeeds`() = runTest {
        coEvery { addUserItemUseCase(any(), any(), any(), any(), any()) } returns
            Result.success(testUserItem)

        viewModel.onAddToWatchlist(testMedia)
        advanceUntilIdle()

        assertEquals("movie_123", viewModel.state.value.justAddedId)
        assertNull(viewModel.state.value.error)
    }
}
