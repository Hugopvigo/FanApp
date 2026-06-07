package com.mediatracker.presentation.discover

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.usecase.GetTrendingUseCase
import com.mediatracker.domain.usecase.SearchMediaUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
class DiscoverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val searchMediaUseCase = mockk<SearchMediaUseCase>()
    private val getTrendingUseCase = mockk<GetTrendingUseCase>()
    private lateinit var viewModel: DiscoverViewModel

    private fun testMediaItem(id: String = "tv_1") = MediaItem(
        id = id,
        mediaType = MediaType.SERIES,
        title = "Test Series",
        overview = "Overview",
        posterUrl = "",
        releaseDate = "2020",
        rating = 8.0f,
        genres = emptyList(),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getTrendingUseCase(any()) } returns Result.success(emptyList())
        viewModel = DiscoverViewModel(searchMediaUseCase, getTrendingUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads trending for SERIES`() = runTest {
        advanceUntilIdle()

        assertEquals(MediaType.SERIES, viewModel.state.value.selectedTab)
        coVerify { getTrendingUseCase(MediaType.SERIES) }
    }

    @Test
    fun `onTabSelected loads trending for new tab`() = runTest {
        advanceUntilIdle()

        viewModel.onTabSelected(MediaType.MOVIE)
        advanceUntilIdle()

        assertEquals(MediaType.MOVIE, viewModel.state.value.selectedTab)
        coVerify { getTrendingUseCase(MediaType.MOVIE) }
    }

    @Test
    fun `onSearchQueryChanged triggers search`() = runTest {
        val results = listOf(testMediaItem())
        coEvery { searchMediaUseCase(any(), any()) } returns Result.success(results)

        viewModel.onSearchQueryChanged("breaking")
        advanceUntilIdle()

        assertEquals("breaking", viewModel.state.value.searchQuery)
        assertEquals(1, viewModel.state.value.searchResults.size)
    }

    @Test
    fun `onSearchQueryChanged with blank query clears results`() = runTest {
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.searchResults.isEmpty())
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `search failure sets error`() = runTest {
        coEvery { searchMediaUseCase(any(), any()) } returns Result.failure(Exception("Network error"))

        viewModel.onSearchQueryChanged("test")
        advanceUntilIdle()

        assertEquals("Network error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `onTabSelected clears search query and results`() = runTest {
        coEvery { searchMediaUseCase(any(), any()) } returns Result.success(listOf(testMediaItem()))
        viewModel.onSearchQueryChanged("test")
        advanceUntilIdle()

        viewModel.onTabSelected(MediaType.BOOK)
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.searchResults.isEmpty())
    }
}
