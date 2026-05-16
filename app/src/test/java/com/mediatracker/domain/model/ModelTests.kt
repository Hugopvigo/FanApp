package com.mediatracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `MediaItem creates with all fields`() {
        val item = MediaItem(
            id = "123",
            title = "Test Movie",
            type = MediaType.MOVIE,
            overview = "A test overview",
            posterUrl = "https://example.com/poster.jpg",
            backdropUrl = "https://example.com/backdrop.jpg",
            releaseYear = 2024,
            rating = 8.5f,
            extraData = mapOf("genre" to "Action"),
        )

        assertEquals("123", item.id)
        assertEquals("Test Movie", item.title)
        assertEquals(MediaType.MOVIE, item.type)
        assertEquals("A test overview", item.overview)
        assertEquals(2024, item.releaseYear)
        assertEquals(8.5f, item.rating)
        assertEquals("Action", item.extraData?.get("genre"))
    }
}

class UserItemTest {

    @Test
    fun `UserItem creates with default values`() {
        val item = UserItem(
            mediaId = "123",
            mediaType = MediaType.SERIES,
            status = ItemStatus.WATCHLIST,
        )

        assertEquals("123", item.mediaId)
        assertEquals(MediaType.SERIES, item.mediaType)
        assertEquals(ItemStatus.WATCHLIST, item.status)
        assertFalse(item.isFavorite)
        assertNull(item.notes)
        assertNull(item.currentEpisode)
        assertNull(item.currentSeason)
    }

    @Test
    fun `UserItem creates with all fields`() {
        val item = UserItem(
            mediaId = "123",
            mediaType = MediaType.SERIES,
            status = ItemStatus.IN_PROGRESS,
            isFavorite = true,
            notes = "Great show!",
            currentEpisode = 5,
            currentSeason = 2,
        )

        assertTrue(item.isFavorite)
        assertEquals("Great show!", item.notes)
        assertEquals(5, item.currentEpisode)
        assertEquals(2, item.currentSeason)
    }
}

class MediaItemWithUserStatusTest {

    @Test
    fun `MediaItemWithUserStatus combines media and user data`() {
        val media = MediaItem(
            id = "123",
            title = "Test",
            type = MediaType.MOVIE,
            rating = 7.0f,
        )
        val userItem = UserItem(
            mediaId = "123",
            mediaType = MediaType.MOVIE,
            status = ItemStatus.COMPLETED,
            isFavorite = true,
        )

        val combined = MediaItemWithUserStatus(media, userItem)

        assertEquals("Test", combined.mediaItem.title)
        assertEquals(ItemStatus.COMPLETED, combined.userItem.status)
        assertTrue(combined.userItem.isFavorite)
    }
}
