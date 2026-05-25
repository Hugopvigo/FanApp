package com.mediatracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTypeTest {

    @Test
    fun `MediaType has correct values`() {
        assertEquals(3, MediaType.entries.size)
        assertEquals("SERIES", MediaType.SERIES.name)
        assertEquals("MOVIE", MediaType.MOVIE.name)
        assertEquals("BOOK", MediaType.BOOK.name)
    }
}

class ItemStatusTest {

    @Test
    fun `ItemStatus has correct values`() {
        assertEquals(4, ItemStatus.entries.size)
        assertEquals("WATCHLIST", ItemStatus.WATCHLIST.name)
        assertEquals("IN_PROGRESS", ItemStatus.IN_PROGRESS.name)
        assertEquals("COMPLETED", ItemStatus.COMPLETED.name)
        assertEquals("ABANDONED", ItemStatus.ABANDONED.name)
    }
}

class MediaItemTest {

    @Test
    fun `MediaItem creates with required fields`() {
        val item = MediaItem(
            id = "tv_123",
            mediaType = MediaType.SERIES,
            title = "Breaking Bad",
            overview = "A chemistry teacher turns to making meth",
            posterUrl = "https://image.tmdb.org/t/p/w500/example.jpg",
            releaseDate = "2008-01-20",
            rating = 9.5f,
            genres = listOf("Drama", "Crime"),
        )

        assertEquals("tv_123", item.id)
        assertEquals(MediaType.SERIES, item.mediaType)
        assertEquals("Breaking Bad", item.title)
        assertEquals("A chemistry teacher turns to making meth", item.overview)
        assertEquals("https://image.tmdb.org/t/p/w500/example.jpg", item.posterUrl)
        assertEquals("2008-01-20", item.releaseDate)
        assertEquals(9.5f, item.rating)
        assertEquals(listOf("Drama", "Crime"), item.genres)
        assertNull(item.extraData)
    }

    @Test
    fun `MediaItem creates with extraData`() {
        val item = MediaItem(
            id = "movie_456",
            mediaType = MediaType.MOVIE,
            title = "Inception",
            overview = "A thief who steals corporate secrets",
            posterUrl = "https://image.tmdb.org/t/p/w500/inception.jpg",
            releaseDate = "2010-07-16",
            rating = 8.8f,
            genres = listOf("Sci-Fi", "Action"),
            extraData = mapOf("director" to "Christopher Nolan", "runtime" to "148"),
        )

        assertEquals("movie_456", item.id)
        assertEquals(MediaType.MOVIE, item.mediaType)
        assertEquals("Christopher Nolan", item.extraData?.get("director"))
        assertEquals("148", item.extraData?.get("runtime"))
    }
}

class UserItemTest {

    @Test
    fun `UserItem creates with all fields`() {
        val item = UserItem(
            id = "item_1",
            mediaType = MediaType.SERIES,
            apiId = "tv_1396",
            title = "Breaking Bad",
            posterUrl = "https://image.tmdb.org/t/p/w500/bb.jpg",
            status = ItemStatus.IN_PROGRESS,
            favorite = true,
            addedAt = 1700000000000L,
            updatedAt = 1700100000000L,
        )

        assertEquals("item_1", item.id)
        assertEquals(MediaType.SERIES, item.mediaType)
        assertEquals("tv_1396", item.apiId)
        assertEquals("Breaking Bad", item.title)
        assertEquals(ItemStatus.IN_PROGRESS, item.status)
        assertTrue(item.favorite)
        assertEquals(1700000000000L, item.addedAt)
        assertEquals(1700100000000L, item.updatedAt)
    }

    @Test
    fun `UserItem favorite defaults to false`() {
        val item = UserItem(
            id = "item_2",
            mediaType = MediaType.BOOK,
            apiId = "book_xyz",
            title = "Book Title",
            posterUrl = null,
            status = ItemStatus.WATCHLIST,
            favorite = false,
            addedAt = 0L,
            updatedAt = 0L,
        )

        assertFalse(item.favorite)
        assertNull(item.posterUrl)
    }
}

class MediaItemWithUserStatusTest {

    @Test
    fun `MediaItemWithUserStatus combines media and user status`() {
        val media = MediaItem(
            id = "tv_1396",
            mediaType = MediaType.SERIES,
            title = "Breaking Bad",
            overview = "A chemistry teacher turns to making meth",
            posterUrl = "https://image.tmdb.org/t/p/w500/bb.jpg",
            releaseDate = "2008-01-20",
            rating = 9.5f,
            genres = listOf("Drama"),
        )
        val userStatus = UserItem(
            id = "item_1",
            mediaType = MediaType.SERIES,
            apiId = "tv_1396",
            title = "Breaking Bad",
            posterUrl = "https://image.tmdb.org/t/p/w500/bb.jpg",
            status = ItemStatus.COMPLETED,
            favorite = true,
            addedAt = 1700000000000L,
            updatedAt = 1700100000000L,
        )

        val combined = MediaItemWithUserStatus(media, userStatus)

        assertEquals("Breaking Bad", combined.media.title)
        assertEquals(ItemStatus.COMPLETED, combined.userStatus?.status)
        assertTrue(combined.userStatus?.favorite ?: false)
    }

    @Test
    fun `MediaItemWithUserStatus allows null user status`() {
        val media = MediaItem(
            id = "movie_456",
            mediaType = MediaType.MOVIE,
            title = "Inception",
            overview = "A thief who steals corporate secrets",
            posterUrl = "https://image.tmdb.org/t/p/w500/inc.jpg",
            releaseDate = "2010-07-16",
            rating = 8.8f,
            genres = listOf("Sci-Fi"),
        )

        val combined = MediaItemWithUserStatus(media, null)

        assertEquals("Inception", combined.media.title)
        assertNull(combined.userStatus)
    }
}
