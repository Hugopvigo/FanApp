package com.mediatracker.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.domain.model.Achievement
import com.mediatracker.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    getAchievementsUseCase: GetAchievementsUseCase,
) : ViewModel() {

    val state: StateFlow<AchievementsUiState> = getAchievementsUseCase()
        .map { list ->
            AchievementsUiState(
                achievements = list,
                unlockedCount = list.count { it.unlockedAt != null },
                totalCount = list.size,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AchievementsUiState())
}
