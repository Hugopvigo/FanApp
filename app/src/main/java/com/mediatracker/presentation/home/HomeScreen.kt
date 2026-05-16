package com.mediatracker.presentation.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.MediaRow

@Composable
fun HomeScreen(
    onItemClick: (com.mediatracker.domain.model.MediaItem) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
