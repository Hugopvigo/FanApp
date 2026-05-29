package com.mediatracker.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.usecase.GetUserItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class StatsUiState(
    val totalCompleted: Int = 0,
    val totalInProgress: Int = 0,
    val totalAbandoned: Int = 0,
    val seriesCompleted: Int = 0,
    val moviesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val estimatedHours: Int = 0,
    val monthlyActivity: List<MonthlyActivity> = emptyList(),
)

data class MonthlyActivity(
    val monthLabel: String,
    val count: Int,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getUserItemsUseCase: GetUserItemsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            getUserItemsUseCase.observeAll()
                .catch { _state.value = StatsUiState() }
                .collect { items ->
                val completed = items.filter { it.status == ItemStatus.COMPLETED }
                val inProgress = items.filter { it.status == ItemStatus.IN_PROGRESS }
                val abandoned = items.filter { it.status == ItemStatus.ABANDONED }

                _state.update {
                    it.copy(
                        totalCompleted = completed.size,
                        totalInProgress = inProgress.size,
                        totalAbandoned = abandoned.size,
                        seriesCompleted = completed.count { it.mediaType == MediaType.SERIES },
                        moviesCompleted = completed.count { it.mediaType == MediaType.MOVIE },
                        booksCompleted = completed.count { it.mediaType == MediaType.BOOK },
                        estimatedHours = calculateEstimatedHours(items),
                        monthlyActivity = calculateMonthlyActivity(items),
                    )
                }
            }
        }
    }

    private fun calculateEstimatedHours(items: List<UserItem>): Int {
        var hours = 0
        for (item in items) {
            if (item.status == ItemStatus.ABANDONED) continue
            when (item.mediaType) {
                MediaType.SERIES -> {
                    val episodes = (item.currentSeason ?: 1) * 10
                    hours += episodes * 45 / 60
                }
                MediaType.MOVIE -> hours += 2
                MediaType.BOOK -> hours += 300 * 2 / 60
            }
        }
        return hours
    }

    private fun calculateMonthlyActivity(items: List<UserItem>): List<MonthlyActivity> {
        val now = Calendar.getInstance()
        val fmt = SimpleDateFormat("MMM", Locale.getDefault())
        val result = mutableListOf<MonthlyActivity>()

        for (i in 11 downTo 0) {
            val cal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -i) }
            val monthLabel = fmt.format(cal.time)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            val count = items.count { item ->
                val itemCal = Calendar.getInstance().apply { timeInMillis = item.addedAt }
                itemCal.get(Calendar.MONTH) == month &&
                    itemCal.get(Calendar.YEAR) == year &&
                    item.status == ItemStatus.COMPLETED
            }
            result.add(MonthlyActivity(monthLabel, count))
        }
        return result
    }
}
