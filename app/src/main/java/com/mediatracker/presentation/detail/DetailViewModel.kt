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

    fun onSeasonEpisodeChanged(season: Int?, episode: Int?) {
        val currentUserItem = _state.value.userItem ?: return
        viewModelScope.launch {
            updateSeasonEpisodeUseCase(currentUserItem.id, season, episode)
        }
    }
}
