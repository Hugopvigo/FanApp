package com.mediatracker.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import com.mediatracker.domain.usecase.SyncUserItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val selectedStatus: ItemStatus = ItemStatus.WATCHLIST,
    val selectedMediaType: MediaType? = null,
    val items: List<UserItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getUserItemsUseCase: GetUserItemsUseCase,
    private val syncUserItemsUseCase: SyncUserItemsUseCase,
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow(ItemStatus.WATCHLIST)
    private val _selectedMediaType = MutableStateFlow<MediaType?>(null)

    val state: StateFlow<LibraryUiState> = combine(
        _selectedStatus,
        _selectedMediaType,
        getUserItemsUseCase.observeAll(),
    ) { status, mediaType, items ->
        val filtered = items.filter { item ->
            item.status == status && (mediaType == null || item.mediaType == mediaType)
        }
        LibraryUiState(
            selectedStatus = status,
            selectedMediaType = mediaType,
            items = filtered,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    init {
        syncItems()
    }

    private fun syncItems() {
        viewModelScope.launch {
            syncUserItemsUseCase()
        }
    }

    fun onStatusSelected(status: ItemStatus) {
        _selectedStatus.value = status
    }

    fun onMediaTypeSelected(mediaType: MediaType?) {
        _selectedMediaType.value = mediaType
    }
}
