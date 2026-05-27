package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
) {
    suspend operator fun invoke(itemId: String): Result<Unit> =
        userRepository.toggleFavorite(itemId)
            .also { if (it.isSuccess) checkAchievementsUseCase() }
}
