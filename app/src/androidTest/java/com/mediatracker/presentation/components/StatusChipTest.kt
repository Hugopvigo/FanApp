package com.mediatracker.presentation.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.presentation.theme.MediaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatusChipTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun statusChip_propagaClic() {
        var clicked = false
        composeRule.setContent {
            MediaTrackerTheme {
                StatusChip(
                    status = ItemStatus.WATCHLIST,
                    selected = false,
                    onClick = { clicked = true },
                )
            }
        }
        composeRule.onRoot().performClick()
        assertTrue(clicked)
    }
}
