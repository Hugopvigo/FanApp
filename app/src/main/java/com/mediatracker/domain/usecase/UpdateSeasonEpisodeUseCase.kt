package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class UpdateSeasonEpisodeUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(itemId: String, season: Int?, episode: Int?): Result<Unit> =
        userRepository.updateSeasonEpisode(itemId, season, episode)
}
