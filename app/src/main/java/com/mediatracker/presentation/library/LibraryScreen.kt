package com.mediatracker.presentation.library

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.LoadingState

@Composable
fun LibraryScreen(
    onItemClick: (UserItem) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        PrimaryScrollableTabRow(
            selectedTabIndex = ItemStatus.entries.indexOf(state.selectedStatus),
            edgePadding = 16.dp,
        ) {
            ItemStatus.entries.forEach { status ->
                Tab(
                    selected = state.selectedStatus == status,
                    onClick = { viewModel.onStatusSelected(status) },
                    text = {
                        Text(
                            when (status) {
                                ItemStatus.WATCHLIST -> stringResource(R.string.library_watchlist)
                                ItemStatus.IN_PROGRESS -> stringResource(R.string.library_in_progress)
                                ItemStatus.COMPLETED -> stringResource(R.string.library_completed)
                                ItemStatus.ABANDONED -> stringResource(R.string.library_abandoned)
                            }
                        )
                    },
                )
            }
        }

        MediaTypeFilterRow(
            selectedMediaType = state.selectedMediaType,
            onMediaTypeSelected = viewModel::onMediaTypeSelected,
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isLoading -> LoadingState()
            state.items.isEmpty() -> EmptyState(
                message = when (state.selectedStatus) {
                    ItemStatus.WATCHLIST -> stringResource(R.string.library_empty_watchlist)
                    ItemStatus.IN_PROGRESS -> stringResource(R.string.library_empty_in_progress)
                    ItemStatus.COMPLETED -> stringResource(R.string.library_empty_completed)
                    ItemStatus.ABANDONED -> stringResource(R.string.library_empty_abandoned)
                }
            )
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id }) { userItem ->
                        LibraryItemCard(
                            userItem = userItem,
                            onClick = { onItemClick(userItem) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaTypeFilterRow(
    selectedMediaType: MediaType?,
    onMediaTypeSelected: (MediaType?) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedMediaType == null,
            onClick = { onMediaTypeSelected(null) },
            label = { Text(stringResource(R.string.library_all)) },
        )
        MediaType.entries.forEach { type ->
            val label = when (type) {
                MediaType.SERIES -> stringResource(R.string.discover_series)
                MediaType.MOVIE -> stringResource(R.string.discover_movies)
                MediaType.BOOK -> stringResource(R.string.discover_books)
            }
            FilterChip(
                selected = selectedMediaType == type,
                onClick = { onMediaTypeSelected(type) },
                label = { Text(label) },
            )
        }
    }
}


