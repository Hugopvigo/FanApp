package com.mediatracker.presentation.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.theme.MediaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MediaCardTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun mediaCard_muestraTituloYPropagaClic() {
        var clicked = false
        val item = MediaItem(
            id = "1", mediaType = MediaType.MOVIE, title = "Inception",
            overview = "", posterUrl = "", releaseDate = "2010",
            rating = 8.8f, genres = emptyList(),
        )
        composeRule.setContent {
            MediaTrackerTheme {
                MediaCard(item = item, onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithText("Inception").performClick()
        assertTrue(clicked)
    }
}
