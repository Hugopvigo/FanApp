package com.mediatracker.di

import android.content.Context
import com.mediatracker.BuildConfig
import com.mediatracker.data.remote.books.GoogleBooksApi
import com.mediatracker.data.remote.books.GoogleBooksRateLimiter
import com.mediatracker.data.remote.books.OpenLibraryApi
import com.mediatracker.data.remote.tmdb.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    private const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"
    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

    const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    fun buildCache(directory: File): Cache = Cache(directory, CACHE_SIZE_BYTES)

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache =
        buildCache(context.cacheDir.resolve("http_cache"))

    private val cacheControlInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (chain.request().method == "GET" &&
            response.code == 200 &&
            response.header("Cache-Control") == null
        ) {
            response.newBuilder()
                .header("Cache-Control", "public, max-age=3600")
                .build()
        } else {
            response
        }
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Named("tmdb_api_key")
    @Singleton
    fun provideTmdbApiKeyInterceptor(): Interceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
            .build()
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        chain.proceed(newRequest)
    }

    @Provides
    @Named("tmdb")
    @Singleton
    fun provideTmdbOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("tmdb_api_key") apiKeyInterceptor: Interceptor,
        cache: Cache,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(cacheControlInterceptor)
        .cache(cache)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Named("books")
    @Singleton
    fun provideBooksOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cache: Cache,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(GoogleBooksRateLimiter())
        .addInterceptor(cacheControlInterceptor)
        .cache(cache)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Named("tmdb")
    @Singleton
    fun provideTmdbRetrofit(
        @Named("tmdb") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Named("books")
    @Singleton
    fun provideBooksRetrofit(
        @Named("books") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(GOOGLE_BOOKS_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Named("openLibrary")
    @Singleton
    fun provideOpenLibraryOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cache: Cache,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(cacheControlInterceptor)
        .cache(cache)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Named("openLibrary")
    @Singleton
    fun provideOpenLibraryRetrofit(
        @Named("openLibrary") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(OPEN_LIBRARY_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTmdbApi(@Named("tmdb") retrofit: Retrofit): TmdbApi =
        retrofit.create(TmdbApi::class.java)

    @Provides
    @Singleton
    fun provideGoogleBooksApi(@Named("books") retrofit: Retrofit): GoogleBooksApi =
        retrofit.create(GoogleBooksApi::class.java)

    @Provides
    @Singleton
    fun provideOpenLibraryApi(@Named("openLibrary") retrofit: Retrofit): OpenLibraryApi =
        retrofit.create(OpenLibraryApi::class.java)
}
