package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class RemoveUserItemUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String): Result<Unit> =
        userRepository.removeUserItem(itemId)
}
