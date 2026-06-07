package com.mediatracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.usecase.AddUserItemUseCase
import com.mediatracker.domain.usecase.GetMediaDetailUseCase
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import com.mediatracker.domain.usecase.RemoveUserItemUseCase
import com.mediatracker.domain.usecase.ToggleFavoriteUseCase
import com.mediatracker.domain.usecase.UpdateItemStatusUseCase
import com.mediatracker.domain.usecase.UpdatePageProgressUseCase
import com.mediatracker.domain.usecase.UpdateSeasonEpisodeUseCase
import com.mediatracker.domain.usecase.UpdateUserNotesUseCase
import com.mediatracker.domain.usecase.UpdateUserRatingUseCase
import com.mediatracker.domain.usecase.GetTvSeasonEpisodeCountUseCase
import com.mediatracker.domain.usecase.UpdateWatchedEpisodesUseCase
import com.mediatracker.data.analytics.AnalyticsHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val addUserItemUseCase = mockk<AddUserItemUseCase>(relaxed = true)
    private val updateItemStatusUseCase = mockk<UpdateItemStatusUseCase>(relaxed = true)
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)
    private val removeUserItemUseCase = mockk<RemoveUserItemUseCase>(relaxed = true)
    private val updateUserRatingUseCase = mockk<UpdateUserRatingUseCase>(relaxed = true)
    private val updateUserNotesUseCase = mockk<UpdateUserNotesUseCase>(relaxed = true)
    private val updatePageProgressUseCase = mockk<UpdatePageProgressUseCase>(relaxed = true)
    private val analytics = mockk<AnalyticsHelper>(relaxed = true)

    private lateinit var getMediaDetailUseCase: GetMediaDetailUseCase
    private lateinit var getUserItemsUseCase: GetUserItemsUseCase
    private lateinit var updateWatchedEpisodesUseCase: UpdateWatchedEpisodesUseCase
    private lateinit var updateSeasonEpisodeUseCase: UpdateSeasonEpisodeUseCase
    private lateinit var getTvSeasonEpisodeCountUseCase: GetTvSeasonEpisodeCountUseCase
    private lateinit var itemsFlow: MutableStateFlow<List<UserItem>>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMediaDetailUseCase = mockk()
        getUserItemsUseCase = mockk()
        updateWatchedEpisodesUseCase = mockk()
        updateSeasonEpisodeUseCase = mockk()
        getTvSeasonEpisodeCountUseCase = mockk()
        coEvery { getTvSeasonEpisodeCountUseCase(any(), any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun seriesUserItem(
        currentSeason: Int = 1,
        currentEpisode: Int = 1,
        watchedEpisodes: Map<Int, List<Int>> = emptyMap(),
    ) = UserItem(
        id = "ui_1",
        mediaType = MediaType.SERIES,
        apiId = "test123",
        title = "Test Series",
        posterUrl = null,
        status = ItemStatus.IN_PROGRESS,
        favorite = false,
        addedAt = 0L,
        updatedAt = 0L,
        currentSeason = currentSeason,
        currentEpisode = currentEpisode,
        watchedEpisodes = watchedEpisodes,
    )

    private fun seriesMediaItem(numberOfSeasons: Int = 2, numberOfEpisodes: Int = 20) = MediaItem(
        id = "series_test123",
        mediaType = MediaType.SERIES,
        title = "Test Series",
        overview = "",
        posterUrl = "",
        releaseDate = "2020",
        rating = 8.0f,
        genres = emptyList(),
        extraData = mapOf(
            "numberOfSeasons" to numberOfSeasons.toString(),
            "numberOfEpisodes" to numberOfEpisodes.toString(),
        ),
    )

    private fun createViewModel(
        userItem: UserItem?,
        mediaItem: MediaItem = seriesMediaItem(),
    ): DetailViewModel {
        itemsFlow = MutableStateFlow(listOfNotNull(userItem))
        every { getUserItemsUseCase.observeAll() } returns itemsFlow
        coEvery { getMediaDetailUseCase(any(), any()) } returns Result.success(mediaItem)
        coEvery { updateWatchedEpisodesUseCase(any(), any()) } returns Result.success(Unit)
        coEvery { updateSeasonEpisodeUseCase(any(), any(), any()) } returns Result.success(Unit)

        return DetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("apiId" to "test123", "mediaType" to "SERIES")),
            getMediaDetailUseCase = getMediaDetailUseCase,
            getUserItemsUseCase = getUserItemsUseCase,
            addUserItemUseCase = addUserItemUseCase,
            updateItemStatusUseCase = updateItemStatusUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            removeUserItemUseCase = removeUserItemUseCase,
            updateUserRatingUseCase = updateUserRatingUseCase,
            updateUserNotesUseCase = updateUserNotesUseCase,
            updateSeasonEpisodeUseCase = updateSeasonEpisodeUseCase,
            updatePageProgressUseCase = updatePageProgressUseCase,
            updateWatchedEpisodesUseCase = updateWatchedEpisodesUseCase,
            getTvSeasonEpisodeCountUseCase = getTvSeasonEpisodeCountUseCase,
            analytics = analytics,
        )
    }

    // Bug 1: marking an earlier episode must not regress currentEpisode
    @Test
    fun `onEpisodeToggle marking earlier episode does not regress currentEpisode`() = runTest {
        val viewModel = createViewModel(
            userItem = seriesUserItem(
                currentSeason = 1,
                currentEpisode = 7,
                watchedEpisodes = mapOf(1 to listOf(1, 2, 4, 5, 6, 7)),
            ),
        )
        advanceUntilIdle()

        viewModel.onEpisodeToggle(season = 1, episode = 3)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateSeasonEpisodeUseCase("ui_1", 1, 3) }
    }

    // Regression: marking a new later episode must still advance currentEpisode
    @Test
    fun `onEpisodeToggle marking later episode advances currentEpisode`() = runTest {
        val viewModel = createViewModel(
            userItem = seriesUserItem(
                currentSeason = 1,
                currentEpisode = 3,
                watchedEpisodes = mapOf(1 to listOf(1, 2, 3)),
            ),
        )
        advanceUntilIdle()

        viewModel.onEpisodeToggle(season = 1, episode = 5)
        advanceUntilIdle()

        coVerify { updateSeasonEpisodeUseCase("ui_1", 1, 5) }
    }

    // Bug 4: suggestSeasonAdvance must clear when season is no longer complete
    @Test
    fun `onEpisodeToggle unchecking episode after season complete clears suggestSeasonAdvance`() = runTest {
        // 2 seasons, 20 total eps → epsPerSeason = 10. User has eps 1-9 of S1.
        val userItemWith9 = seriesUserItem(
            currentSeason = 1,
            currentEpisode = 9,
            watchedEpisodes = mapOf(1 to (1..9).toList()),
        )
        val viewModel = createViewModel(userItem = userItemWith9, mediaItem = seriesMediaItem(2, 20))
        advanceUntilIdle()

        // Mark ep 10 → season complete → suggestSeasonAdvance = true
        viewModel.onEpisodeToggle(season = 1, episode = 10)
        advanceUntilIdle()
        assertTrue("Expected suggestSeasonAdvance=true after completing season", viewModel.state.value.suggestSeasonAdvance)

        // Simulate the DB write propagating back through the Flow
        itemsFlow.value = listOf(
            userItemWith9.copy(
                currentEpisode = 10,
                watchedEpisodes = mapOf(1 to (1..10).toList()),
            ),
        )
        advanceUntilIdle()

        // Uncheck ep 10 → season incomplete → suggestSeasonAdvance must clear
        viewModel.onEpisodeToggle(season = 1, episode = 10)
        advanceUntilIdle()

        assertFalse("Expected suggestSeasonAdvance=false after unchecking last episode", viewModel.state.value.suggestSeasonAdvance)
    }
}
