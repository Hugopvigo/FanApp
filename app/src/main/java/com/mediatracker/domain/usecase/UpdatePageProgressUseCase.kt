package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdatePageProgressUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String, currentPage: Int?, totalPages: Int?): Result<Unit> =
        userRepository.updatePageProgress(itemId, currentPage, totalPages)
}
