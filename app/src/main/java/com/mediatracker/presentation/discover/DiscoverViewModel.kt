package com.mediatracker.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.usecase.GetTrendingUseCase
import com.mediatracker.domain.usecase.SearchMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val selectedTab: MediaType = MediaType.SERIES,
    val searchQuery: String = "",
    val searchResults: List<MediaItem> = emptyList(),
    val trending: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingTrending: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val searchMediaUseCase: SearchMediaUseCase,
    private val getTrendingUseCase: GetTrendingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    init {
        loadTrending(MediaType.SERIES)
    }

    fun onTabSelected(tab: MediaType) {
        _state.update { it.copy(selectedTab = tab, searchQuery = "", searchResults = emptyList(), error = null) }
        loadTrending(tab)
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), error = null) }
            return
        }
        search(query)
    }

    private fun search(query: String) {
        val mediaType = _state.value.selectedTab
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            searchMediaUseCase(query, mediaType)
                .onSuccess { results ->
                    _state.update { it.copy(searchResults = results, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun loadTrending(mediaType: MediaType) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTrending = true) }
            getTrendingUseCase(mediaType)
                .onSuccess { results ->
                    _state.update { it.copy(trending = results, isLoadingTrending = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoadingTrending = false, error = error.message) }
                }
        }
    }
}
