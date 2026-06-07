package com.mediatracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.mediatracker.domain.usecase.UpdateUserRatingUseCase
import com.mediatracker.domain.usecase.UpdateUserNotesUseCase
import com.mediatracker.domain.usecase.UpdateSeasonEpisodeUseCase
import com.mediatracker.domain.usecase.UpdatePageProgressUseCase
import com.mediatracker.domain.usecase.GetTvSeasonEpisodeCountUseCase
import com.mediatracker.domain.usecase.UpdateWatchedEpisodesUseCase
import com.mediatracker.data.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val item: MediaItem? = null,
    val userItem: UserItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val suggestBookComplete: Boolean = false,
    val suggestSeasonAdvance: Boolean = false,
    val suggestSeriesComplete: Boolean = false,
    val seasonEpisodeCount: Int? = null,
    val showGoToPageDialog: Boolean = false,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMediaDetailUseCase: GetMediaDetailUseCase,
    private val getUserItemsUseCase: GetUserItemsUseCase,
    private val addUserItemUseCase: AddUserItemUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val removeUserItemUseCase: RemoveUserItemUseCase,
    private val updateUserRatingUseCase: UpdateUserRatingUseCase,
    private val updateUserNotesUseCase: UpdateUserNotesUseCase,
    private val updateSeasonEpisodeUseCase: UpdateSeasonEpisodeUseCase,
    private val updatePageProgressUseCase: UpdatePageProgressUseCase,
    private val updateWatchedEpisodesUseCase: UpdateWatchedEpisodesUseCase,
    private val getTvSeasonEpisodeCountUseCase: GetTvSeasonEpisodeCountUseCase,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private var watchedBackfillDone = false
    private var lastLoadedSeason: Int? = null

    private val apiId: String = savedStateHandle["apiId"] ?: ""
    private val mediaType: MediaType = runCatching {
        savedStateHandle.get<MediaType>("mediaType") ?: MediaType.SERIES
    }.recoverCatching {
        savedStateHandle.get<String>("mediaType")?.let { MediaType.valueOf(it) } ?: MediaType.SERIES
    }.getOrDefault(MediaType.SERIES)

    private val compositeId: String = "${mediaType.name.lowercase()}_$apiId"

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
        observeUserItem()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            getMediaDetailUseCase(compositeId, mediaType)
                .onSuccess { item ->
                    _state.update { it.copy(item = item, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun observeUserItem() {
        viewModelScope.launch {
            getUserItemsUseCase.observeAll().collect { items ->
                val userItem = items.find { it.apiId == apiId && it.mediaType == mediaType }
                _state.update { it.copy(userItem = userItem) }
                userItem?.let {
                    maybeBackfillWatchedEpisodes(it)
                    loadSeasonEpisodeCount(it)
                }
            }
        }
    }

    private suspend fun maybeBackfillWatchedEpisodes(userItem: UserItem) {
        if (watchedBackfillDone || userItem.mediaType != MediaType.SERIES) return
        if (userItem.status != ItemStatus.IN_PROGRESS) return
        val season = userItem.currentSeason ?: return
        val episode = userItem.currentEpisode ?: return
        if (episode <= 0 || userItem.watchedEpisodes[season]?.isNotEmpty() == true) {
            watchedBackfillDone = true
            return
        }
        watchedBackfillDone = true
        val backfill = (1..episode).toList()
        val updated = userItem.watchedEpisodes.toMutableMap().apply { put(season, backfill) }
        updateWatchedEpisodesUseCase(userItem.id, updated)
        updateSeasonEpisodeUseCase(userItem.id, season, episode)
    }

    private fun loadSeasonEpisodeCount(userItem: UserItem) {
        if (userItem.mediaType != MediaType.SERIES || userItem.status != ItemStatus.IN_PROGRESS) return
        val season = userItem.currentSeason ?: 1
        if (season == lastLoadedSeason && _state.value.seasonEpisodeCount != null) return
        lastLoadedSeason = season
        viewModelScope.launch {
            getTvSeasonEpisodeCountUseCase(apiId, season)
                .onSuccess { count ->
                    _state.update { it.copy(seasonEpisodeCount = count.coerceAtLeast(1)) }
                }
                .onFailure {
                    val item = _state.value.item
                    val numberOfSeasons = item?.extraData?.get("numberOfSeasons")?.toIntOrNull()
                    val numberOfEpisodes = item?.extraData?.get("numberOfEpisodes")?.toIntOrNull()
                    val fallback = if (numberOfSeasons != null && numberOfEpisodes != null && numberOfSeasons > 0) {
                        numberOfEpisodes / numberOfSeasons
                    } else null
                    _state.update { it.copy(seasonEpisodeCount = fallback) }
                }
        }
    }

    private fun episodesPerSeasonForSeason(season: Int, seasonEps: List<Int>): Int {
        val fromApi = _state.value.seasonEpisodeCount
        if (fromApi != null && fromApi > 0) return fromApi
        val item = _state.value.item ?: return 0
        val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
        val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
        return if (numberOfSeasons != null && numberOfEpisodes != null && numberOfSeasons > 0) {
            numberOfEpisodes / numberOfSeasons
        } else {
            seasonEps.maxOrNull() ?: 1
        }
    }

    fun onStatusSelected(status: ItemStatus) {
        val currentUserItem = _state.value.userItem
        val currentItem = _state.value.item // capture at click time, not inside coroutine
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val userItemId = if (currentUserItem == null) {
                addUserItemUseCase(
                    mediaType = mediaType,
                    apiId = apiId,
                    title = currentItem?.title ?: "",
                    posterUrl = currentItem?.posterUrl,
                    status = status,
                ).getOrNull()?.id
            } else {
                updateItemStatusUseCase(currentUserItem.id, status)
                currentUserItem.id
            }
            if (status == ItemStatus.IN_PROGRESS && mediaType == MediaType.BOOK && userItemId != null) {
                val pageCount = currentItem?.extraData?.get("pageCount")?.toIntOrNull()
                val existingTotal = currentUserItem?.totalPages
                if (pageCount != null && pageCount > 0 && existingTotal == null) {
                    updatePageProgressUseCase(userItemId, currentUserItem?.currentPage, pageCount)
                }
            }
            _state.update { it.copy(isUpdating = false) }
        }
    }

    fun onRemoveFromList() {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            removeUserItemUseCase(currentUserItem.id)
            _state.update { it.copy(isUpdating = false) }
        }
    }

    fun onToggleFavorite() {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            toggleFavoriteUseCase(currentUserItem.id)
        }
    }

    fun onRatingChanged(rating: Int?) {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updateUserRatingUseCase(currentUserItem.id, rating)
        }
    }

    fun onNotesChanged(notes: String) {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updateUserNotesUseCase(currentUserItem.id, notes)
        }
    }

    fun onOpenGoToPageDialog() {
        _state.update { it.copy(showGoToPageDialog = true) }
    }

    fun onDismissGoToPageDialog() {
        _state.update { it.copy(showGoToPageDialog = false) }
    }

    fun onGoToPageConfirm(pageInput: String) {
        val page = pageInput.trim().toIntOrNull() ?: return
        val userItem = _state.value.userItem ?: return
        val total = userItem.totalPages
            ?: _state.value.item?.extraData?.get("pageCount")?.toIntOrNull()
            ?: 0
        if (page < 0) return
        if (total > 0 && page > total) return
        onDismissGoToPageDialog()
        onPageProgressChanged(page, total.takeIf { it > 0 })
    }

    fun onPageProgressChanged(currentPage: Int?, totalPages: Int?) {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updatePageProgressUseCase(currentUserItem.id, currentPage, totalPages)
            val tp = totalPages ?: _state.value.item?.extraData?.get("pageCount")?.toIntOrNull()
            if (tp != null && tp > 0 && currentPage != null) {
                analytics.logPageProgress((currentPage * 100 / tp))
            }
            if (tp != null && tp > 0 && currentPage != null && currentPage >= tp) {
                _state.update { it.copy(suggestBookComplete = true) }
            }
        }
    }

    fun onConfirmBookComplete() {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updateItemStatusUseCase(currentUserItem.id, ItemStatus.COMPLETED)
            _state.update { it.copy(suggestBookComplete = false) }
        }
    }

    fun onDismissBookComplete() {
        _state.update { it.copy(suggestBookComplete = false) }
    }

    fun onSeasonEpisodeChanged(season: Int?, episode: Int?) {
        val currentUserItem = _state.value.userItem ?: return
        if (season != null && season != lastLoadedSeason) {
            lastLoadedSeason = null
            _state.update { it.copy(seasonEpisodeCount = null) }
        }
        viewModelScope.launch {
            updateSeasonEpisodeUseCase(currentUserItem.id, season, episode)
            val item = _state.value.item ?: return@launch
            val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
            val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
            val s = season ?: return@launch
            val e = episode ?: return@launch
            if (numberOfSeasons != null && s >= numberOfSeasons && numberOfEpisodes != null) {
                val avgEpsPerSeason = numberOfEpisodes / numberOfSeasons
                if (e >= avgEpsPerSeason) {
                    _state.update { it.copy(suggestSeriesComplete = true) }
                }
            } else if (numberOfSeasons != null && s < numberOfSeasons && numberOfEpisodes != null) {
                val avgEpsPerSeason = numberOfEpisodes / numberOfSeasons
                if (e >= avgEpsPerSeason) {
                    _state.update { it.copy(suggestSeasonAdvance = true) }
                }
            }
        }
    }

    fun onConfirmSeasonAdvance() {
        val currentUserItem = _state.value.userItem ?: return
        val nextSeason = (currentUserItem.currentSeason ?: 1) + 1
        viewModelScope.launch {
            updateSeasonEpisodeUseCase(currentUserItem.id, nextSeason, 1)
            _state.update { it.copy(suggestSeasonAdvance = false) }
        }
    }

    fun onDismissSeasonAdvance() {
        _state.update { it.copy(suggestSeasonAdvance = false) }
    }

    fun onConfirmSeriesComplete() {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updateItemStatusUseCase(currentUserItem.id, ItemStatus.COMPLETED)
            _state.update { it.copy(suggestSeriesComplete = false) }
        }
    }

    fun onDismissSeriesComplete() {
        _state.update { it.copy(suggestSeriesComplete = false) }
    }

    fun onEpisodeToggle(season: Int, episode: Int) {
        val currentUserItem = _state.value.userItem ?: return
        val watched = currentUserItem.watchedEpisodes.toMutableMap()
        val seasonEps = watched[season]?.toMutableList() ?: mutableListOf()
        val isCurrentSeason = season == (currentUserItem.currentSeason ?: 1)

        if (episode in seasonEps) {
            seasonEps.remove(episode)
        } else {
            seasonEps.add(episode)
            seasonEps.sort()
        }

        if (seasonEps.isEmpty()) {
            watched.remove(season)
        } else {
            watched[season] = seasonEps
        }

        val isNowWatched = episode in seasonEps
        val newEpisode = if (isCurrentSeason) {
            if (isNowWatched) maxOf(episode, currentUserItem.currentEpisode ?: 1)
            else seasonEps.maxOrNull() ?: 1
        } else {
            currentUserItem.currentEpisode ?: 1
        }

        viewModelScope.launch {
            updateWatchedEpisodesUseCase(currentUserItem.id, watched)
            if (isCurrentSeason && newEpisode != currentUserItem.currentEpisode) {
                updateSeasonEpisodeUseCase(currentUserItem.id, season, newEpisode)
            }
            analytics.logEpisodeToggle(currentUserItem.mediaType.name)

            val item = _state.value.item ?: return@launch
            val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
            val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
            val epsPerSeason = episodesPerSeasonForSeason(season, seasonEps)

            val allSeasonWatched = epsPerSeason > 0 && (1..epsPerSeason).all { it in seasonEps }
            if (allSeasonWatched) {
                if (numberOfSeasons != null && season >= numberOfSeasons) {
                    _state.update { it.copy(suggestSeriesComplete = true) }
                } else {
                    _state.update { it.copy(suggestSeasonAdvance = true) }
                }
            } else {
                _state.update { it.copy(suggestSeasonAdvance = false, suggestSeriesComplete = false) }
            }
        }
    }
}
