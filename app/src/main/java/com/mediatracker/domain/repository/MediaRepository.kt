package com.mediatracker.domain.repository

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType

interface MediaRepository {
    suspend fun search(query: String, mediaType: MediaType): Result<List<MediaItem>>
    suspend fun getTrending(mediaType: MediaType): Result<List<MediaItem>>
    suspend fun getDetail(id: String, mediaType: MediaType): Result<MediaItem>
    suspend fun getTvSeasonEpisodeCount(tvApiId: String, seasonNumber: Int): Result<Int>
}
