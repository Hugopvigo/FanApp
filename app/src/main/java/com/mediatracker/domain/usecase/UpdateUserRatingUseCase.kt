package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserRatingUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String, rating: Int?): Result<Unit> =
        userRepository.updateUserRating(itemId, rating)
}
