package com.mediatracker.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MediaType { SERIES, MOVIE, BOOK }

val MediaType.displayLabel: String
    get() = when (this) {
        MediaType.SERIES -> "Serie"
        MediaType.MOVIE -> "Película"
        MediaType.BOOK -> "Libro"
    }
