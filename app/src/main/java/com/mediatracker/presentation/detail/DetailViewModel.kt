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
import com.mediatracker.domain.usecase.UpdateWatchedEpisodesUseCase
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
) : ViewModel() {

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
            }
        }
    }

    fun onStatusSelected(status: ItemStatus) {
        val currentUserItem = _state.value.userItem
        val currentItem = _state.value.item // capture at click time, not inside coroutine
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            if (currentUserItem == null) {
                addUserItemUseCase(
                    mediaType = mediaType,
                    apiId = apiId,
                    title = currentItem?.title ?: "",
                    posterUrl = currentItem?.posterUrl,
                    status = status,
                )
            } else {
                updateItemStatusUseCase(currentUserItem.id, status)
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

    fun onPageProgressChanged(currentPage: Int?, totalPages: Int?) {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updatePageProgressUseCase(currentUserItem.id, currentPage, totalPages)
            val tp = totalPages ?: _state.value.item?.extraData?.get("pageCount")?.toIntOrNull()
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

        val maxWatched = seasonEps.maxOrNull()
        val newEpisode = if (season == (currentUserItem.currentSeason ?: 1)) {
            maxWatched ?: 1
        } else {
            currentUserItem.currentEpisode ?: 1
        }

        viewModelScope.launch {
            updateWatchedEpisodesUseCase(currentUserItem.id, watched)
            if (newEpisode != currentUserItem.currentEpisode || season != currentUserItem.currentSeason) {
                updateSeasonEpisodeUseCase(currentUserItem.id, season, newEpisode)
            }

            val item = _state.value.item ?: return@launch
            val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
            val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
            val avgEpsPerSeason = if (numberOfSeasons != null && numberOfEpisodes != null && numberOfSeasons > 0) {
                numberOfEpisodes / numberOfSeasons
            } else 0

            if (avgEpsPerSeason > 0 && seasonEps.size >= avgEpsPerSeason) {
                if (numberOfSeasons != null && season >= numberOfSeasons) {
                    _state.update { it.copy(suggestSeriesComplete = true) }
                } else {
                    _state.update { it.copy(suggestSeasonAdvance = true) }
                }
            }
        }
    }
}
