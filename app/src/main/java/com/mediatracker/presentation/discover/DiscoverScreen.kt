package com.mediatracker.presentation.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.ErrorState
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.MediaCard
import com.mediatracker.presentation.components.MediaRow
import com.mediatracker.presentation.components.SearchBar
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DiscoverScreen(
    onItemClick: (MediaItem) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiscoverScreenContent(
        state = state,
        onItemClick = onItemClick,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onTabSelected = viewModel::onTabSelected,
    )
}

@Composable
private fun DiscoverScreenContent(
    state: DiscoverUiState,
    onItemClick: (MediaItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (MediaType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.discover_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        SearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabSelector(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected,
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null && state.searchResults.isEmpty() -> ErrorState(state.error ?: stringResource(R.string.error_unknown))
            state.searchQuery.isNotBlank() && state.searchResults.isEmpty() -> EmptyState(stringResource(R.string.no_results))
            state.searchQuery.isBlank() -> {
                Column {
                    Text(
                        text = stringResource(R.string.discover_top_trending),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    if (state.isLoadingTrending) {
                        LoadingState()
                    } else {
                        MediaRow(
                            items = state.trending,
                            onItemClick = onItemClick,
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.searchResults, key = { it.id }) { item ->
                        MediaCard(
                            item = item,
                            onClick = { onItemClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 300)
@Composable
private fun DiscoverScreenPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        DiscoverScreenContent(
            state = DiscoverUiState(
                selectedTab = MediaType.SERIES,
                trending = listOf(
                    MediaItem("t1", MediaType.SERIES, "Stranger Things", "", "", "2016", 8.7f, listOf("Fantasy")),
                    MediaItem("t2", MediaType.MOVIE, "Inception", "", "", "2010", 8.8f, listOf("Sci-Fi")),
                ),
                isLoadingTrending = false,
            ),
            onItemClick = {},
            onSearchQueryChange = {},
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoverScreenSearchPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        DiscoverScreenContent(
            state = DiscoverUiState(
                selectedTab = MediaType.BOOK,
                searchQuery = "Harry Potter",
                searchResults = listOf(
                    MediaItem("b1", MediaType.BOOK, "Harry Potter y la piedra filosofal", "", "", "1997", 4.5f, listOf("Fantasy")),
                    MediaItem("b2", MediaType.BOOK, "Harry Potter y la cámara secreta", "", "", "1998", 4.4f, listOf("Fantasy")),
                    MediaItem("b3", MediaType.BOOK, "Harry Potter y el prisionero de Azkaban", "", "", "1999", 4.6f, listOf("Fantasy")),
                ),
                isLoading = false,
            ),
            onItemClick = {},
            onSearchQueryChange = {},
            onTabSelected = {},
        )
    }
}

@Composable
private fun TabSelector(
    selectedTab: MediaType,
    onTabSelected: (MediaType) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MediaType.entries.forEach { type ->
            val label = when (type) {
                MediaType.SERIES -> stringResource(R.string.discover_series)
                MediaType.MOVIE -> stringResource(R.string.discover_movies)
                MediaType.BOOK -> stringResource(R.string.discover_books)
            }
            FilterChip(
                selected = selectedTab == type,
                onClick = { onTabSelected(type) },
                label = { Text(label) },
            )
        }
    }
}
