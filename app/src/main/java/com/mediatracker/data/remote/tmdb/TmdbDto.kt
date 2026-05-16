package com.mediatracker.data.remote.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSearchResponse(
    val page: Int = 0,
    val results: List<TmdbItemDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class TmdbItemDto(
    val id: Int = 0,
    @SerialName("media_type") val mediaType: String = "",
    val name: String? = null,
    val title: String? = null,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Float = 0f,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("original_language") val originalLanguage: String = "",
)

@Serializable
data class TmdbTvDetailDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Float = 0f,
    @SerialName("first_air_date") val firstAirDate: String = "",
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int = 0,
    @SerialName("created_by") val createdBy: List<TmdbCreatorDto> = emptyList(),
    val status: String = "",
)

@Serializable
data class TmdbMovieDetailDto(
    val id: Int = 0,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Float = 0f,
    @SerialName("release_date") val releaseDate: String = "",
    val genres: List<TmdbGenreDto> = emptyList(),
    val runtime: Int? = null,
    val status: String = "",
    @SerialName("original_language") val originalLanguage: String = "",
)

@Serializable
data class TmdbGenreDto(
    val id: Int = 0,
    val name: String = "",
)

@Serializable
data class TmdbCreatorDto(
    val id: Int = 0,
    val name: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
)
