package com.mediatracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.usecase.GetMediaDetailUseCase
import com.mediatracker.domain.usecase.GetTrendingUseCase
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String? = null,
    val trending: List<MediaItem> = emptyList(),
    val continueWatching: List<MediaItem> = emptyList(),
    val favorites: List<MediaItem> = emptyList(),
    val recent: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrendingUseCase: GetTrendingUseCase,
    private val getUserItemsUseCase: GetUserItemsUseCase,
    private val getMediaDetailUseCase: GetMediaDetailUseCase,
    private val authDataSource: AuthDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeUserName()
        loadTrending()
        observeUserItems()
    }

    private fun observeUserName() {
        viewModelScope.launch {
            authDataSource.authStateFlow()
                .map { it.userName }
                .collect { name -> _state.update { it.copy(userName = name) } }
        }
    }

    private fun loadTrending() {
        viewModelScope.launch {
            val allTrending = MediaType.entries.flatMap { type ->
                getTrendingUseCase(type).getOrDefault(emptyList())
            }
            _state.update { it.copy(trending = allTrending, isLoading = false) }
        }
    }

    private fun observeUserItems() {
        viewModelScope.launch {
            getUserItemsUseCase.observeAll().collect { items ->
                val inProgress = items.filter { it.status == ItemStatus.IN_PROGRESS }
                val favs = items.filter { it.favorite }
                val recentItems = items.sortedByDescending { it.addedAt }.take(10)
                loadMediaDetails(inProgress, favs, recentItems)
            }
        }
    }

    private fun loadMediaDetails(
        inProgress: List<UserItem>,
        favs: List<UserItem>,
        recentItems: List<UserItem>,
    ) {
        viewModelScope.launch {
            val continueWatching = inProgress.mapNotNull { getMediaItem(it) }
            val favorites = favs.mapNotNull { getMediaItem(it) }
            val recent = recentItems.mapNotNull { getMediaItem(it) }
            _state.update { it.copy(continueWatching = continueWatching, favorites = favorites, recent = recent) }
        }
    }

    private suspend fun getMediaItem(userItem: UserItem): MediaItem? {
        val compositeId = "${userItem.mediaType.name.lowercase()}_${userItem.apiId}"
        return getMediaDetailUseCase(compositeId, userItem.mediaType).getOrNull()
    }
}
