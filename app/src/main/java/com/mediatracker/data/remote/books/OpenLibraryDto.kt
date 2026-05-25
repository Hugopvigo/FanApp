package com.mediatracker.data.remote.books

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenLibraryTrendingResponse(
    val works: List<OpenLibraryWork> = emptyList(),
)

@Serializable
data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryWork> = emptyList(),
    @SerialName("numFound") val totalFound: Int = 0,
)

@Serializable
data class OpenLibraryWork(
    val key: String = "",
    val title: String = "",
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Int? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("ratings_average") val ratingsAverage: Double? = null,
    @SerialName("ratings_count") val ratingsCount: Int? = null,
    val subject: List<String>? = null,
)
