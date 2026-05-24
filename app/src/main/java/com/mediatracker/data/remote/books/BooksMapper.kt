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
    rating = normalizeRating(volumeInfo.averageRating),
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
    rating = normalizeRating(volumeInfo.averageRating),
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

/**
 * Google Books returns ratings on a 0–5 scale. We normalize to 0–10
 * so the UI can display "X.X / 10" consistently with TMDB.
 */
private fun normalizeRating(rating: Float): Float = (rating * 2f).coerceAtMost(10f)

/**
 * Extracts the publication year from a Google Books publishedDate string.
 * The field can be "2017", "2017-05-15", or even just a year range.
 * Returns null if parsing fails.
 */
fun extractPublicationYear(publishedDate: String): Int? =
    publishedDate.take(4).toIntOrNull()

/**
 * Filters a list of BookItemDto to remove low-quality results:
 * - Items without a cover image
 * - Items older than [minYear] (only if [minYear] is provided)
 */
fun List<BookItemDto>.filterQualityBooks(minYear: Int? = null): List<BookItemDto> =
    filter { item ->
        val hasCover = item.volumeInfo.imageLinks != null
        val yearOk = minYear?.let { min ->
            val year = extractPublicationYear(item.volumeInfo.publishedDate)
            year != null && year >= min
        } ?: true
        hasCover && yearOk
    }
