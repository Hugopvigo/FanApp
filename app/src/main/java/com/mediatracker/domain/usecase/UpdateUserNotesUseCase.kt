package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserNotesUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String, notes: String): Result<Unit> =
        userRepository.updateUserNotes(itemId, notes)
}
