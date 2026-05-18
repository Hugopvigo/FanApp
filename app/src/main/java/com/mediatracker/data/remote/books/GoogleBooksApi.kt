package com.mediatracker.data.remote.books

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("langRestrict") langRestrict: String = "es",
        @Query("maxResults") maxResults: Int = 20,
        @Query("startIndex") startIndex: Int = 0,
        @Query("key") key: String = "",
    ): BooksSearchResponse

    @GET("volumes")
    suspend fun getPopularBooks(
        @Query("q") query: String = "subject:fiction+subject:bestseller",
        @Query("orderBy") orderBy: String = "relevance",
        @Query("langRestrict") langRestrict: String = "es",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") key: String = "",
    ): BooksSearchResponse

    @GET("volumes/{volume_id}")
    suspend fun getBookDetail(
        @Path("volume_id") volumeId: String,
        @Query("key") key: String = "",
    ): BookDetailDto
}
