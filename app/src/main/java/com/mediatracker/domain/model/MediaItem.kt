package com.mediatracker.domain.model

data class MediaItem(
    val id: String,
    val mediaType: MediaType,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val releaseDate: String,
    val rating: Float,
    val genres: List<String>,
    val extraData: Map<String, String>? = null,
)
