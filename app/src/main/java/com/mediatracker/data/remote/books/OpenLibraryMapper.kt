package com.mediatracker.data.remote.books

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType

private const val OL_COVERS_BASE = "https://covers.openlibrary.org/b/id/"

fun OpenLibraryWork.toMediaItem(): MediaItem {
    val workId = key.removePrefix("/works/")
    val coverUrl = coverId?.let { "${OL_COVERS_BASE}${it}-L.jpg" } ?: ""
    // Open Library rates on 0-5; normalize to 0-10 consistent with TMDB/Google Books
    val rating = ratingsAverage?.let { (it * 2.0).toFloat().coerceIn(0f, 10f) } ?: 0f

    return MediaItem(
        id = "book_ol_$workId",
        mediaType = MediaType.BOOK,
        title = title,
        overview = authorName?.joinToString(", ") ?: "",
        posterUrl = coverUrl,
        releaseDate = firstPublishYear?.toString() ?: "",
        rating = rating,
        genres = subject?.take(3) ?: emptyList(),
        extraData = buildMap {
            authorName?.joinToString(", ")?.let { put("authors", it) }
        },
    )
}

fun List<OpenLibraryWork>.toMediaItems(): List<MediaItem> =
    filter { it.title.isNotBlank() && it.coverId != null }
        .map { it.toMediaItem() }
