package com.mediatracker.domain.usecase

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class UserStats(
    val seriesInProgress: Int = 0,
    val seriesCompleted: Int = 0,
    val moviesInProgress: Int = 0,
    val moviesCompleted: Int = 0,
    val booksInProgress: Int = 0,
    val booksCompleted: Int = 0,
)

class GetUserStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<UserStats> =
        userRepository.getUserItemsFlow().map { items ->
            val activeItems = items.filter { it.status != ItemStatus.ABANDONED }
            UserStats(
                seriesInProgress = activeItems.count { it.mediaType == MediaType.SERIES && it.status == ItemStatus.IN_PROGRESS },
                seriesCompleted = activeItems.count { it.mediaType == MediaType.SERIES && it.status == ItemStatus.COMPLETED },
                moviesInProgress = activeItems.count { it.mediaType == MediaType.MOVIE && it.status == ItemStatus.IN_PROGRESS },
                moviesCompleted = activeItems.count { it.mediaType == MediaType.MOVIE && it.status == ItemStatus.COMPLETED },
                booksInProgress = activeItems.count { it.mediaType == MediaType.BOOK && it.status == ItemStatus.IN_PROGRESS },
                booksCompleted = activeItems.count { it.mediaType == MediaType.BOOK && it.status == ItemStatus.COMPLETED },
            )
        }
}
