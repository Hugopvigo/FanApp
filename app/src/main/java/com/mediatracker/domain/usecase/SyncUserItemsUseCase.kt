package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class SyncUserItemsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<Unit> = userRepository.syncUserItems()
}
