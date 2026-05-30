package com.mediatracker.domain.usecase

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateItemStatusUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    private val requestInAppReviewUseCase: RequestInAppReviewUseCase,
) {
    suspend operator fun invoke(itemId: String, status: ItemStatus): Result<Unit> =
        userRepository.updateItemStatus(itemId, status)
            .also {
                if (it.isSuccess) {
                    checkAchievementsUseCase()
                    if (status == ItemStatus.COMPLETED) {
                        requestInAppReviewUseCase.maybeQueueReviewTrigger()
                    }
                }
            }
}
