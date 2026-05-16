package com.mediatracker.data.remote.tmdb

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500"

fun TmdbItemDto.toMediaItem(mediaType: MediaType): MediaItem = MediaItem(
    id = "${mediaType.name.lowercase()}_$id",
    mediaType = mediaType,
    title = name ?: title ?: "",
    overview = overview,
    posterUrl = posterPath?.let { "$TMDB_IMAGE_BASE$it" } ?: "",
    releaseDate = firstAirDate ?: releaseDate ?: "",
    rating = voteAverage,
    genres = genreIds.map { it.toString() },
)

fun TmdbTvDetailDto.toMediaItem(): MediaItem = MediaItem(
    id = "series_$id",
    mediaType = MediaType.SERIES,
    title = name,
    overview = overview,
    posterUrl = posterPath?.let { "$TMDB_IMAGE_BASE$it" } ?: "",
    releaseDate = firstAirDate,
    rating = voteAverage,
    genres = genres.map { it.name },
    extraData = mapOf(
        "numberOfSeasons" to numberOfSeasons.toString(),
        "numberOfEpisodes" to numberOfEpisodes.toString(),
        "creators" to createdBy.joinToString(", ") { it.name },
        "status" to status,
    ),
)

fun TmdbMovieDetailDto.toMediaItem(): MediaItem = MediaItem(
    id = "movie_$id",
    mediaType = MediaType.MOVIE,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { "$TMDB_IMAGE_BASE$it" } ?: "",
    releaseDate = releaseDate,
    rating = voteAverage,
    genres = genres.map { it.name },
    extraData = buildMap {
        runtime?.let { put("runtime", it.toString()) }
        put("status", status)
    },
)
