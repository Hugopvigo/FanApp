package com.mediatracker.data.remote.books

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("trending/daily.json")
    suspend fun getTrendingBooks(
        @Query("limit") limit: Int = 20,
    ): OpenLibraryTrendingResponse

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,cover_i,ratings_average,ratings_count,subject",
    ): OpenLibrarySearchResponse
}
