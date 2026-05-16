package com.mediatracker.domain.usecase

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.repository.MediaRepository
import javax.inject.Inject

class GetMediaDetailUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(id: String, mediaType: MediaType): Result<MediaItem> =
        mediaRepository.getDetail(id, mediaType)
}
