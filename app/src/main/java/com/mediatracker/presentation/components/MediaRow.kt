package com.mediatracker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun MediaRow(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items, key = { it.id }) { item ->
            MediaCard(
                item = item,
                onClick = { onItemClick(item) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaRowPreview() {
    val sampleItems = listOf(
        MediaItem("s1", MediaType.SERIES, "Breaking Bad", "", "", "2008", 9.5f, listOf("Drama", "Thriller")),
        MediaItem("s2", MediaType.MOVIE, "Inception", "", "", "2010", 8.8f, listOf("Sci-Fi", "Action")),
        MediaItem("s3", MediaType.BOOK, "1984", "", "", "1949", 4.6f, listOf("Dystopian", "Fiction")),
        MediaItem("s4", MediaType.SERIES, "Stranger Things", "", "", "2016", 8.7f, listOf("Horror", "Fantasy")),
    )
    MediaTrackerTheme {
        MediaRow(items = sampleItems, onItemClick = {})
    }
}
