package com.mediatracker.data.remote.books

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("langRestrict") langRestrict: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("startIndex") startIndex: Int = 0,
        @Query("printType") printType: String = "books",
        @Query("projection") projection: String = "lite",
        @Query("key") key: String = "",
    ): BooksSearchResponse

    @GET("volumes")
    suspend fun getPopularBooks(
        @Query("q") query: String = "subject:fiction",
        @Query("orderBy") orderBy: String = "newest",
        @Query("maxResults") maxResults: Int = 20,
        @Query("langRestrict") langRestrict: String,
        @Query("printType") printType: String = "books",
        @Query("filter") filter: String = "partial",
        @Query("key") key: String = "",
    ): BooksSearchResponse

    @GET("volumes/{volume_id}")
    suspend fun getBookDetail(
        @Path("volume_id") volumeId: String,
        @Query("projection") projection: String = "full",
        @Query("key") key: String = "",
    ): BookDetailDto
}
