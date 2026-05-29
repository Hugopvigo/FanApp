package com.mediatracker.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.DetailedStats
import com.mediatracker.domain.model.GenreCount
import com.mediatracker.domain.model.MonthlyActivityPoint
import com.mediatracker.domain.usecase.GetDetailedStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias MonthlyActivity = MonthlyActivityPoint

data class StatsUiState(
    val totalCompleted: Int = 0,
    val totalInProgress: Int = 0,
    val totalAbandoned: Int = 0,
    val totalWatchlist: Int = 0,
    val seriesCompleted: Int = 0,
    val moviesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val estimatedHours: Int = 0,
    val topGenres: List<GenreCount> = emptyList(),
    val monthlyActivity: List<MonthlyActivity> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDetailedStatsUseCase: GetDetailedStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            getDetailedStatsUseCase()
                .catch { _state.value = StatsUiState() }
                .collect { stats -> _state.update { stats.toUiState() } }
        }
    }

    private fun DetailedStats.toUiState() = StatsUiState(
        totalCompleted = totalCompleted,
        totalInProgress = totalInProgress,
        totalAbandoned = totalAbandoned,
        totalWatchlist = totalWatchlist,
        seriesCompleted = seriesCompleted,
        moviesCompleted = moviesCompleted,
        booksCompleted = booksCompleted,
        estimatedHours = estimatedHours,
        topGenres = topGenres,
        monthlyActivity = monthlyActivity,
    )
}
