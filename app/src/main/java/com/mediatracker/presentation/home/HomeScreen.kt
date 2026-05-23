package com.mediatracker.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.MediaRow
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun HomeScreen(
    onItemClick: (MediaItem) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreenContent(state = state, onItemClick = onItemClick)
}

@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    onItemClick: (MediaItem) -> Unit = {},
) {
    when {
        state.isLoading -> LoadingState()
        state.error != null && state.trending.isEmpty() -> EmptyState(stringResource(R.string.error_generic))
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(8.dp))

                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(stringResource(R.string.home_continue))
                    MediaRow(items = state.continueWatching, onItemClick = onItemClick)
                    Spacer(Modifier.height(16.dp))
                }

                SectionHeader(stringResource(R.string.home_trending))
                if (state.trending.isEmpty()) {
                    EmptyState(stringResource(R.string.empty_list))
                } else {
                    MediaRow(items = state.trending, onItemClick = onItemClick)
                }
                Spacer(Modifier.height(16.dp))

                if (state.favorites.isNotEmpty()) {
                    SectionHeader(stringResource(R.string.home_favorites))
                    MediaRow(items = state.favorites, onItemClick = onItemClick)
                    Spacer(Modifier.height(16.dp))
                }

                if (state.recent.isNotEmpty()) {
                    SectionHeader(stringResource(R.string.home_recent))
                    MediaRow(items = state.recent, onItemClick = onItemClick)
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun HomeScreenContentPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        HomeScreenContent(
            state = HomeUiState(
                trending = listOf(
                    MediaItem("t1", MediaType.SERIES, "Stranger Things", "", "", "2016", 8.7f, listOf("Fantasy")),
                    MediaItem("t2", MediaType.MOVIE, "Inception", "", "", "2010", 8.8f, listOf("Sci-Fi")),
                    MediaItem("t3", MediaType.BOOK, "1984", "", "", "1949", 4.6f, listOf("Fiction")),
                ),
                continueWatching = listOf(
                    MediaItem("c1", MediaType.SERIES, "Breaking Bad", "", "", "2008", 9.5f, listOf("Drama")),
                ),
                favorites = listOf(
                    MediaItem("f1", MediaType.MOVIE, "The Matrix", "", "", "1999", 8.7f, listOf("Sci-Fi")),
                ),
                recent = listOf(
                    MediaItem("r1", MediaType.BOOK, "Dune", "", "", "1965", 4.3f, listOf("Sci-Fi")),
                    MediaItem("r2", MediaType.SERIES, "Dark", "", "", "2017", 8.5f, listOf("Thriller")),
                ),
                isLoading = false,
            ),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        HomeScreenContent(
            state = HomeUiState(isLoading = true),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        HomeScreenContent(
            state = HomeUiState(trending = emptyList(), isLoading = false, error = "Error test"),
            onItemClick = {},
        )
    }
}
