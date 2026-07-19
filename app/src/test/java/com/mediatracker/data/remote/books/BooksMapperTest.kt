package com.mediatracker.data.remote.books

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksMapperTest {

    private fun book(id: String, title: String, authors: String?) = MediaItem(
        id = id,
        mediaType = MediaType.BOOK,
        title = title,
        overview = "",
        posterUrl = "",
        releaseDate = "",
        rating = 0f,
        genres = emptyList(),
        extraData = authors?.let { mapOf("authors" to it) },
    )

    @Test
    fun `same work from two sources is deduplicated`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_ol_1", "it", "Stephen King"),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `same title by different authors is kept`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_g2", "It", "Alexa Chung"),
        )
        assertEquals(2, items.dedupeBooks().size)
    }

    @Test
    fun `only first author matters for the key`() {
        val items = listOf(
            book("book_g1", "Good Omens", "Terry Pratchett, Neil Gaiman"),
            book("book_g2", "Good Omens", "Terry Pratchett"),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `missing authors falls back to title-only key`() {
        val items = listOf(
            book("book_g1", "Anónimo", null),
            book("book_g2", "Anónimo", null),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `first occurrence wins`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_ol_1", "It", "Stephen King"),
        )
        assertEquals("book_g1", items.dedupeBooks().single().id)
    }
}
