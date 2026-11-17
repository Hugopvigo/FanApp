package com.mediatracker.presentation.fancard

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import org.junit.Assert.assertEquals
import org.junit.Test

class FanCardRatingTest {

    private fun item(rating: Int?) = UserItem(
        id = "x", mediaType = MediaType.MOVIE, apiId = "1",
        title = "Título", posterUrl = null,
        status = ItemStatus.COMPLETED, favorite = false,
        addedAt = 0L, updatedAt = 0L, userRating = rating,
    )

    @Test
    fun `averageRating ignora items sin valorar`() {
        assertEquals(4f, averageRating(listOf(item(3), item(5), item(null))), 0.001f)
    }

    @Test
    fun `averageRating devuelve cero sin valoraciones`() {
        assertEquals(0f, averageRating(listOf(item(null))), 0.001f)
    }

    @Test
    fun `averageRating devuelve cero con lista vacia`() {
        assertEquals(0f, averageRating(emptyList()), 0.001f)
    }
}
