package com.mediatracker.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.auth.AuthDataSource
import com.mediatracker.data.firestore.FirebaseAuthProvider
import com.mediatracker.data.firestore.FirestoreDataSource
import com.mediatracker.domain.model.Ranking
import com.mediatracker.domain.usecase.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LeaderboardUiState(
    val rankings: List<Ranking> = emptyList(),
    val userRank: Int? = null,
    val userEntry: Ranking? = null,
    val currentUserId: String? = null,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val authDataSource: AuthDataSource,
    private val authProvider: FirebaseAuthProvider,
    private val getUserStatsUseCase: GetUserStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    private val stats by lazy {
        getUserStatsUseCase().stateIn(viewModelScope, SharingStarted.Lazily, com.mediatracker.domain.model.UserStats())
    }

    private val categories = listOf("all_time", "yearly", "series", "movies", "books")

    init {
        loadLeaderboard(0)
        syncRanking()
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTab = index, isLoading = true) }
        loadLeaderboard(index)
    }

    private fun loadLeaderboard(tabIndex: Int) {
        val category = categories.getOrElse(tabIndex) { "all_time" }
        viewModelScope.launch {
            firestoreDataSource.getLeaderboard(category)
                .onSuccess { list ->
                    val currentUid = authProvider.userId
                    val userIndex = list.indexOfFirst { it.userId == currentUid }
                    val userRank = userIndex.takeIf { it >= 0 }?.plus(1)
                    val userEntry = userIndex.takeIf { it >= 0 }?.let { list[it] }
                    _state.update {
                        it.copy(
                            rankings = list,
                            userRank = userRank,
                            userEntry = userEntry,
                            currentUserId = currentUid,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load leaderboard")
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun syncRanking() {
        viewModelScope.launch {
            try {
                val snapshot = stats.first()
                val displayName = authDataSource.getUserName() ?: "Anonymous"
                val avatarId = try {
                    firestoreDataSource.getAvatarId().getOrDefault(null)
                } catch (_: Exception) { null }
                firestoreDataSource.updateRanking(
                    displayName = displayName,
                    avatarId = avatarId,
                    totalCompleted = snapshot.seriesCompleted + snapshot.moviesCompleted + snapshot.booksCompleted,
                    seriesCompleted = snapshot.seriesCompleted,
                    moviesCompleted = snapshot.moviesCompleted,
                    booksCompleted = snapshot.booksCompleted,
                    xp = snapshot.totalXp,
                    level = snapshot.level,
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync ranking")
            }
        }
    }
}
