package com.mediatracker.data.remote.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1,
    ): TmdbSearchResponse

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("query") query: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1,
    ): TmdbSearchResponse

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("language") language: String = "es-ES",
    ): TmdbSearchResponse

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "es-ES",
    ): TmdbSearchResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetail(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String = "es-ES",
    ): TmdbTvDetailDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "es-ES",
    ): TmdbMovieDetailDto

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getTvSeason(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String = "es-ES",
    ): TmdbSeasonDetailDto
}
