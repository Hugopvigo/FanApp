package com.mediatracker.presentation.fancard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.DetailedStats
import com.mediatracker.domain.model.UserStats
import com.mediatracker.domain.usecase.GetDetailedStatsUseCase
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import com.mediatracker.domain.usecase.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FanCardType { RATING, TOP_MONTH, PROFILE, STATS }

data class FanCardRatingData(
    val item: UserItem,
    val mediaTitle: String,
    val mediaPosterUrl: String?,
    val rating: Int,
)

data class FanCardUiState(
    val cardType: FanCardType = FanCardType.RATING,
    val stats: UserStats = UserStats(),
    val detailedStats: DetailedStats = DetailedStats(),
    val topCompleted: List<UserItem> = emptyList(),
    val ratingData: FanCardRatingData? = null,
    val avgRating: Float = 0f,
    val userName: String = "Usuario",
)

@HiltViewModel
class FanCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUserStatsUseCase: GetUserStatsUseCase,
    getDetailedStatsUseCase: GetDetailedStatsUseCase,
    getUserItemsUseCase: GetUserItemsUseCase,
    private val authDataSource: AuthDataSource,
) : ViewModel() {

    private val ratingApiId: String? = savedStateHandle["ratingApiId"]
    private val ratingMediaType: MediaType? = savedStateHandle.get<String>("ratingMediaType")
        ?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
    private val ratingValueFallback: Int? = savedStateHandle["ratingValue"]

    private val _userName = MutableStateFlow(authDataSource.getUserName() ?: "Usuario")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        viewModelScope.launch {
            authDataSource.authStateFlow().collect { state ->
                _userName.value = state.userName?.takeIf { it.isNotBlank() } ?: "Usuario"
            }
        }
    }

    val state: StateFlow<FanCardUiState> = combine(
        getUserStatsUseCase(),
        getDetailedStatsUseCase(),
        getUserItemsUseCase.observeAll(),
        _userName,
    ) { stats, detailed, items, name ->
        val completed = items
            .filter { it.status == ItemStatus.COMPLETED }
            .sortedByDescending { it.userRating ?: 0 }
        val rated = completed.filter { it.userRating != null }
        val avg = if (rated.isNotEmpty()) rated.map { it.userRating!!.toFloat() }.average().toFloat() else 0f
        val ratingItem = if (ratingApiId != null && ratingMediaType != null) {
            items.find { it.apiId == ratingApiId && it.mediaType == ratingMediaType }
        } else {
            rated.firstOrNull()
        }
        val ratingStars = ratingItem?.userRating ?: ratingValueFallback
        val ratingData = if (ratingItem != null && ratingStars != null) {
            FanCardRatingData(
                item = ratingItem,
                mediaTitle = ratingItem.title,
                mediaPosterUrl = ratingItem.posterUrl,
                rating = ratingStars,
            )
        } else null
        FanCardUiState(
            stats = stats,
            detailedStats = detailed,
            topCompleted = completed.take(5),
            ratingData = ratingData,
            avgRating = avg,
            userName = name,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FanCardUiState())
}
