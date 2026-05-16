package com.mediatracker.data.remote.books

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType

fun BookItemDto.toMediaItem(): MediaItem = MediaItem(
    id = "book_$id",
    mediaType = MediaType.BOOK,
    title = volumeInfo.title,
    overview = volumeInfo.description ?: "",
    posterUrl = volumeInfo.imageLinks?.toSecureHighResUrl() ?: "",
    releaseDate = volumeInfo.publishedDate,
    rating = volumeInfo.averageRating,
    genres = volumeInfo.categories,
    extraData = buildMap {
        if (volumeInfo.authors.isNotEmpty()) {
            put("authors", volumeInfo.authors.joinToString(", "))
        }
        volumeInfo.publisher?.let { put("publisher", it) }
        if (volumeInfo.pageCount > 0) {
            put("pageCount", volumeInfo.pageCount.toString())
        }
    },
)

fun BookDetailDto.toMediaItem(): MediaItem = MediaItem(
    id = "book_$id",
    mediaType = MediaType.BOOK,
    title = volumeInfo.title,
    overview = volumeInfo.description ?: "",
    posterUrl = volumeInfo.imageLinks?.toSecureHighResUrl() ?: "",
    releaseDate = volumeInfo.publishedDate,
    rating = volumeInfo.averageRating,
    genres = volumeInfo.categories,
    extraData = buildMap {
        if (volumeInfo.authors.isNotEmpty()) {
            put("authors", volumeInfo.authors.joinToString(", "))
        }
        volumeInfo.publisher?.let { put("publisher", it) }
        if (volumeInfo.pageCount > 0) {
            put("pageCount", volumeInfo.pageCount.toString())
        }
        if (volumeInfo.ratingsCount > 0) {
            put("ratingsCount", volumeInfo.ratingsCount.toString())
        }
    },
)

private fun BookImageLinksDto.toSecureHighResUrl(): String? {
    val url = large ?: medium ?: thumbnail ?: small ?: smallThumbnail ?: return null
    return url.replace("http://", "https://").replace("zoom=1", "zoom=2")
}
