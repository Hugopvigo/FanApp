package com.mediatracker.presentation.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaItemWithUserStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import com.mediatracker.domain.usecase.AddUserItemUseCase
import com.mediatracker.domain.usecase.SearchMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAddUiState(
    val query: String = "",
    val selectedTab: MediaType = MediaType.SERIES,
    val results: List<MediaItemWithUserStatus> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val justAddedId: String? = null,
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val searchMediaUseCase: SearchMediaUseCase,
    private val addUserItemUseCase: AddUserItemUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAddUiState())
    val state: StateFlow<QuickAddUiState> = _state.asStateFlow()

    private var searchResults: List<MediaItem> = emptyList()

    init {
        observeUserItems()
    }

    private val userItemsMap = MutableStateFlow<Map<String, UserItem>>(emptyMap())

    private fun observeUserItems() {
        viewModelScope.launch {
            userRepository.getUserItemsFlow().collect { items ->
                userItemsMap.value = items.associateBy { it.apiId }
                mergeResultsWithUserStatus()
            }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query, error = null) }
        if (query.isBlank()) {
            searchResults = emptyList()
            _state.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            searchMediaUseCase(query, _state.value.selectedTab)
                .onSuccess { items ->
                    searchResults = items
                    mergeResultsWithUserStatus()
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onTabSelected(tab: MediaType) {
        _state.update { it.copy(selectedTab = tab, results = emptyList(), isLoading = false, error = null) }
        searchResults = emptyList()
        val q = _state.value.query
        if (q.isNotBlank()) onQueryChanged(q)
    }

    fun onAddToWatchlist(item: MediaItem) {
        val apiId = item.id.removePrefix("${item.mediaType.name.lowercase()}_")
        viewModelScope.launch {
            addUserItemUseCase(
                mediaType = item.mediaType,
                apiId = apiId,
                title = item.title,
                posterUrl = item.posterUrl.ifBlank { null },
                status = ItemStatus.WATCHLIST,
            )
            _state.update { it.copy(justAddedId = item.id) }
        }
    }

    fun clearJustAdded() {
        _state.update { it.copy(justAddedId = null) }
    }

    private fun mergeResultsWithUserStatus() {
        val userMap = userItemsMap.value
        val enriched = searchResults.map { media ->
            val apiId = media.id.removePrefix("${media.mediaType.name.lowercase()}_")
            MediaItemWithUserStatus(media = media, userStatus = userMap[apiId])
        }
        _state.update { it.copy(results = enriched) }
    }
}
