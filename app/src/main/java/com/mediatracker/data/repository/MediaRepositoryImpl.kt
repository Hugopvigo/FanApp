package com.mediatracker.data.repository

import com.mediatracker.BuildConfig
import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.toDomain
import com.mediatracker.data.local.toEntity
import com.mediatracker.data.remote.books.GoogleBooksApi
import com.mediatracker.data.remote.books.toMediaItem
import com.mediatracker.data.remote.tmdb.TmdbApi
import com.mediatracker.data.remote.tmdb.toMediaItem
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.repository.MediaRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val googleBooksApi: GoogleBooksApi,
    private val mediaItemDao: MediaItemDao,
) : MediaRepository {

    companion object {
        private const val TTL_DETAIL_MS = 24 * 60 * 60 * 1000L

        private val BOOKS_TRENDING_QUERIES = listOf(
            "subject:fiction+subject:bestseller",
            "subject:fantasy+subject:adventure",
            "subject:romance+subject:contemporary",
            "subject:thriller+subject:mystery",
            "subject:nonfiction+subject:popular",
        )

        private fun trendingBooksQuery(): String {
            val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            return BOOKS_TRENDING_QUERIES[dayOfYear % BOOKS_TRENDING_QUERIES.size]
        }
    }

    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaItem>> =
        runCatching {
            when (mediaType) {
                MediaType.SERIES -> tmdbApi.searchTv(query).results.map { it.toMediaItem(MediaType.SERIES) }
                MediaType.MOVIE -> tmdbApi.searchMovie(query).results.map { it.toMediaItem(MediaType.MOVIE) }
                MediaType.BOOK -> googleBooksApi.searchBooks(
                    query = query,
                    key = BuildConfig.GOOGLE_BOOKS_API_KEY,
                ).items.map { it.toMediaItem() }
            }.also { items -> cacheItems(items) }
        }.onFailure { Timber.e(it, "Search failed: $query ($mediaType)") }

    override suspend fun getTrending(mediaType: MediaType): Result<List<MediaItem>> =
        runCatching {
            when (mediaType) {
                MediaType.SERIES -> tmdbApi.getTrendingTv().results.map { it.toMediaItem(MediaType.SERIES) }
                MediaType.MOVIE -> tmdbApi.getTrendingMovies().results.map { it.toMediaItem(MediaType.MOVIE) }
                MediaType.BOOK -> googleBooksApi.getPopularBooks(
                    query = trendingBooksQuery(),
                    key = BuildConfig.GOOGLE_BOOKS_API_KEY,
                ).items.map { it.toMediaItem() }
            }.also { items -> cacheItems(items) }
        }.onFailure { Timber.e(it, "Trending failed: $mediaType") }

    override suspend fun getDetail(id: String, mediaType: MediaType): Result<MediaItem> =
        runCatching {
            val cached = mediaItemDao.getById(id)
            if (cached != null && !isExpired(cached.cachedAt, TTL_DETAIL_MS)) {
                return Result.success(cached.toDomain())
            }
            fetchAndCacheDetail(id, mediaType)
        }.onFailure { Timber.e(it, "Detail failed: $id ($mediaType)") }

    private suspend fun fetchAndCacheDetail(id: String, mediaType: MediaType): MediaItem {
        val item = when (mediaType) {
            MediaType.SERIES -> {
                val apiId = id.removePrefix("series_").toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid series id: $id")
                tmdbApi.getTvDetail(apiId).toMediaItem()
            }
            MediaType.MOVIE -> {
                val apiId = id.removePrefix("movie_").toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid movie id: $id")
                tmdbApi.getMovieDetail(apiId).toMediaItem()
            }
            MediaType.BOOK -> {
                val apiId = id.removePrefix("book_")
                googleBooksApi.getBookDetail(apiId, key = BuildConfig.GOOGLE_BOOKS_API_KEY).toMediaItem()
            }
        }
        mediaItemDao.insert(item.toEntity())
        return item
    }

    private suspend fun cacheItems(items: List<MediaItem>) {
        val now = System.currentTimeMillis()
        mediaItemDao.insertAll(items.map { it.toEntity(now) })
    }

    private fun isExpired(cachedAt: Long, ttlMs: Long): Boolean =
        System.currentTimeMillis() - cachedAt > ttlMs
}
