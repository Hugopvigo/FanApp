package com.mediatracker.data.repository

import com.mediatracker.BuildConfig
import com.mediatracker.data.local.LocaleRepository
import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.toDomain
import com.mediatracker.data.local.toEntity
import com.mediatracker.data.remote.books.GoogleBooksApi
import com.mediatracker.data.remote.books.OpenLibraryApi
import com.mediatracker.data.remote.books.buildGoogleBooksQuery
import com.mediatracker.data.remote.books.dedupeBooks
import com.mediatracker.data.remote.books.filterQualityBooks

import com.mediatracker.data.remote.books.toMediaItem
import com.mediatracker.data.remote.books.toMediaItems
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
    private val openLibraryApi: OpenLibraryApi,
    private val mediaItemDao: MediaItemDao,
    private val localeRepository: LocaleRepository,
) : MediaRepository {

    companion object {
        private const val TTL_DETAIL_MS = 24 * 60 * 60 * 1000L
        private const val MIN_BOOK_PUBLICATION_YEAR = 2015
        private const val MIN_BOOKS_THRESHOLD = 5

        private val BOOKS_TRENDING_QUERIES = listOf(
            "subject:fiction",
            "subject:fantasy",
            "subject:\"science fiction\"",
            "subject:romance",
            "subject:thriller",
            "subject:mystery",
            "subject:biography",
        )

        private fun trendingBooksQuery(): String {
            val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            return BOOKS_TRENDING_QUERIES[dayOfYear % BOOKS_TRENDING_QUERIES.size]
        }
    }

    private suspend fun tmdbLang(): String =
        localeRepository.tmdbLanguage(localeRepository.getLanguageCode())

    override suspend fun search(query: String, mediaType: MediaType): Result<List<MediaItem>> =
        runCatching {
            val lang = tmdbLang()
            when (mediaType) {
                MediaType.SERIES -> tmdbApi.searchTv(query, language = lang).results.map { it.toMediaItem(MediaType.SERIES) }
                MediaType.MOVIE  -> tmdbApi.searchMovie(query, language = lang).results.map { it.toMediaItem(MediaType.MOVIE) }
                MediaType.BOOK   -> searchBooks(query)
            }.also { cacheItems(it) }
        }.onFailure { Timber.e(it, "Search failed: $query ($mediaType)") }

    override suspend fun getTrending(mediaType: MediaType): Result<List<MediaItem>> =
        runCatching {
            val lang = tmdbLang()
            when (mediaType) {
                MediaType.SERIES -> tmdbApi.getTrendingTv(language = lang).results.map { it.toMediaItem(MediaType.SERIES) }
                MediaType.MOVIE  -> tmdbApi.getTrendingMovies(language = lang).results.map { it.toMediaItem(MediaType.MOVIE) }
                MediaType.BOOK   -> getTrendingBooks()
            }.also { cacheItems(it) }
        }.onFailure { Timber.e(it, "Trending failed: $mediaType") }

    override suspend fun getDetail(id: String, mediaType: MediaType): Result<MediaItem> =
        runCatching {
            val cached = mediaItemDao.getById(id)
            if (cached != null && !isExpired(cached.cachedAt, TTL_DETAIL_MS)) {
                return Result.success(cached.toDomain())
            }
            fetchAndCacheDetail(id, mediaType)
        }.onFailure { Timber.e(it, "Detail failed: $id ($mediaType)") }

    // ── Books helpers ─────────────────────────────────────────────────────────

    private suspend fun getTrendingBooks(): List<MediaItem> {
        val lang = localeRepository.googleBooksLang(localeRepository.getLanguageCode())
        return try {
            val googleItems = googleBooksApi.getPopularBooks(
                query = trendingBooksQuery(),
                langRestrict = lang,
                key = BuildConfig.GOOGLE_BOOKS_API_KEY,
            ).items.filterQualityBooks(minYear = MIN_BOOK_PUBLICATION_YEAR)
                .map { it.toMediaItem() }

            if (googleItems.size >= MIN_BOOKS_THRESHOLD) {
                googleItems
            } else {
                Timber.d("Google Books trending below threshold (${googleItems.size}), complementing with Open Library")
                val olItems = openLibraryApi.getTrendingBooks().works.toMediaItems()
                (googleItems + olItems)
                    .distinctBy { it.title.lowercase() }
                    .take(20)
            }
        } catch (e: Exception) {
            Timber.w(e, "Google Books trending failed, falling back to Open Library")
            try {
                openLibraryApi.getTrendingBooks().works.toMediaItems()
            } catch (e2: Exception) {
                Timber.e(e2, "Open Library trending also failed")
                emptyList()
            }
        }
    }

    private suspend fun searchBooks(query: String): List<MediaItem> {
        val lang = localeRepository.googleBooksLang(localeRepository.getLanguageCode())
        return try {
            val googleItems = googleBooksApi.searchBooks(
                query = buildGoogleBooksQuery(query),
                langRestrict = lang,
                key = BuildConfig.GOOGLE_BOOKS_API_KEY,
            ).items.filterQualityBooks()
                .map { it.toMediaItem() }
                .dedupeBooks()

            if (googleItems.size >= MIN_BOOKS_THRESHOLD) {
                googleItems
            } else {
                Timber.d("Google Books search below threshold (${googleItems.size}), complementing with Open Library")
                val olItems = openLibraryApi.searchBooks(query = query).docs.toMediaItems()
                (googleItems + olItems)
                    .dedupeBooks()
                    .take(20)
            }
        } catch (e: Exception) {
            Timber.w(e, "Google Books search failed, falling back to Open Library")
            try {
                openLibraryApi.searchBooks(query = query).docs.toMediaItems()
            } catch (e2: Exception) {
                Timber.e(e2, "Open Library search also failed")
                emptyList()
            }
        }
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    private suspend fun fetchAndCacheDetail(id: String, mediaType: MediaType): MediaItem {
        val lang = tmdbLang()
        val item = when (mediaType) {
            MediaType.SERIES -> {
                val apiId = id.removePrefix("series_").toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid series id: $id")
                tmdbApi.getTvDetail(apiId, language = lang).toMediaItem()
            }
            MediaType.MOVIE -> {
                val apiId = id.removePrefix("movie_").toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid movie id: $id")
                tmdbApi.getMovieDetail(apiId, language = lang).toMediaItem()
            }
            MediaType.BOOK -> {
                // Open Library books have id "book_ol_OL..." — use cached item, no detail API yet
                val apiId = id.removePrefix("book_")
                if (apiId.startsWith("ol_")) {
                    // Return cached item if available; otherwise minimal item from cache
                    mediaItemDao.getById(id)?.toDomain()
                        ?: throw IllegalStateException("Open Library book not in cache: $id")
                } else {
                    googleBooksApi.getBookDetail(apiId, key = BuildConfig.GOOGLE_BOOKS_API_KEY).toMediaItem()
                }
            }
        }
        mediaItemDao.insert(item.toEntity())
        return item
    }

    // ── Cache helpers ─────────────────────────────────────────────────────────

    private suspend fun cacheItems(items: List<MediaItem>) {
        val now = System.currentTimeMillis()
        mediaItemDao.insertAll(items.map { it.toEntity(now) })
    }

    override suspend fun getTvSeasonEpisodeCount(tvApiId: String, seasonNumber: Int): Result<Int> =
        runCatching {
            val tvId = tvApiId.toIntOrNull() ?: throw IllegalArgumentException("Invalid tv id: $tvApiId")
            val lang = localeRepository.tmdbLanguage(localeRepository.getLanguageCode())
            val season = tmdbApi.getTvSeason(tvId, seasonNumber, language = lang)
            season.episodes.size.coerceAtLeast(1)
        }.onFailure { Timber.w(it, "TV season fetch failed: $tvApiId S$seasonNumber") }

    private fun isExpired(cachedAt: Long, ttlMs: Long): Boolean =
        System.currentTimeMillis() - cachedAt > ttlMs
}
