package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateWatchedEpisodesUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String, watchedEpisodes: Map<Int, List<Int>>): Result<Unit> =
        userRepository.updateWatchedEpisodes(itemId, watchedEpisodes)
}
