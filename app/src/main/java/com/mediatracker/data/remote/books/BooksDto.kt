package com.mediatracker.data.remote.books

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BooksSearchResponse(
    @SerialName("totalItems") val totalItems: Int = 0,
    val items: List<BookItemDto> = emptyList(),
)

@Serializable
data class BookItemDto(
    val id: String = "",
    val volumeInfo: BookVolumeInfoDto = BookVolumeInfoDto(),
)

@Serializable
data class BookVolumeInfoDto(
    val title: String = "",
    val subtitle: String? = null,
    val authors: List<String> = emptyList(),
    val publisher: String? = null,
    @SerialName("publishedDate") val publishedDate: String = "",
    val description: String? = null,
    @SerialName("pageCount") val pageCount: Int = 0,
    val categories: List<String> = emptyList(),
    @SerialName("averageRating") val averageRating: Float = 0f,
    @SerialName("ratingsCount") val ratingsCount: Int = 0,
    @SerialName("imageLinks") val imageLinks: BookImageLinksDto? = null,
    @SerialName("language") val language: String = "",
)

@Serializable
data class BookImageLinksDto(
    @SerialName("smallThumbnail") val smallThumbnail: String? = null,
    val thumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
)

@Serializable
data class BookDetailDto(
    val id: String = "",
    val volumeInfo: BookVolumeInfoDto = BookVolumeInfoDto(),
)
