package com.mediatracker.domain.usecase

import com.mediatracker.domain.repository.MediaRepository
import javax.inject.Inject

class GetTvSeasonEpisodeCountUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(tvApiId: String, seasonNumber: Int): Result<Int> =
        mediaRepository.getTvSeasonEpisodeCount(tvApiId, seasonNumber)
}
