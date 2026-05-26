package com.mediatracker.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.LibraryGridSkeleton
import com.mediatracker.presentation.components.fanColor
import com.mediatracker.presentation.components.statusIcon
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors

@Composable
fun LibraryScreen(
    onItemClick: (UserItem) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LibraryScreenContent(
        state = state,
        onItemClick = onItemClick,
        onStatusSelected = viewModel::onStatusSelected,
        onMediaTypeSelected = viewModel::onMediaTypeSelected,
    )
}

@Composable
private fun LibraryScreenContent(
    state: LibraryUiState,
    onItemClick: (UserItem) -> Unit,
    onStatusSelected: (ItemStatus) -> Unit,
    onMediaTypeSelected: (MediaType?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.statusBarsPadding())

        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.library_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily,
                    letterSpacing = (-0.4).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${state.items.size} items",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Media type filter chips ───────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                LibraryChip(
                    text = stringResource(R.string.library_all),
                    active = state.selectedMediaType == null,
                    onClick = { onMediaTypeSelected(null) },
                )
            }
            items(MediaType.entries) { type ->
                val label = when (type) {
                    MediaType.SERIES -> stringResource(R.string.discover_series)
                    MediaType.MOVIE  -> stringResource(R.string.discover_movies)
                    MediaType.BOOK   -> stringResource(R.string.discover_books)
                }
                val icon = when (type) {
                    MediaType.SERIES -> "🎬"
                    MediaType.MOVIE  -> "🎥"
                    MediaType.BOOK   -> "📖"
                }
                LibraryChip(
                    text = "$icon $label",
                    active = state.selectedMediaType == type,
                    onClick = { onMediaTypeSelected(type) },
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Status color tabs ─────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(ItemStatus.entries) { status ->
                StatusTab(
                    status = status,
                    selected = state.selectedStatus == status,
                    count = if (state.selectedStatus == status) state.items.size else null,
                    onClick = { onStatusSelected(status) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> LibraryGridSkeleton()
            state.items.isEmpty() -> EmptyState(
                message = when (state.selectedStatus) {
                    ItemStatus.WATCHLIST   -> stringResource(R.string.library_empty_watchlist)
                    ItemStatus.IN_PROGRESS -> stringResource(R.string.library_empty_in_progress)
                    ItemStatus.COMPLETED   -> stringResource(R.string.library_empty_completed)
                    ItemStatus.ABANDONED   -> stringResource(R.string.library_empty_abandoned)
                },
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

// ─── Neutral glass chip (media type filter) ───────────────────────────────────
@Composable
private fun LibraryChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (active) Brush.linearGradient(fanColors.gradientAccent)
                else Brush.linearGradient(listOf(fanColors.surfaceGlass, fanColors.surfaceGlass)),
            )
            .border(1.dp, if (active) Color.Transparent else fanColors.borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─── Color-coded status tab ───────────────────────────────────────────────────
@Composable
private fun StatusTab(
    status: ItemStatus,
    selected: Boolean,
    count: Int?,
    onClick: () -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val statusColor = status.fanColor(fanColors)
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) statusColor.copy(alpha = if (fanColors.isDark) 0.25f else 0.18f)
                else Color.Transparent,
            )
            .border(
                width = 1.dp,
                color = if (selected) statusColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(text = status.statusIcon, fontSize = 11.sp)
            Text(
                text = when (status) {
                    ItemStatus.WATCHLIST   -> stringResource(R.string.library_watchlist)
                    ItemStatus.IN_PROGRESS -> stringResource(R.string.library_in_progress)
                    ItemStatus.COMPLETED   -> stringResource(R.string.library_completed)
                    ItemStatus.ABANDONED   -> stringResource(R.string.library_abandoned)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (selected && count != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                    )
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, heightDp = 600)
@Composable
private fun LibraryScreenContentPreview() {
    val now = System.currentTimeMillis()
    val sampleItems = listOf(
        UserItem("u1", MediaType.SERIES, "tv_1", "Breaking Bad", null, ItemStatus.IN_PROGRESS, true, now, now),
        UserItem("u2", MediaType.MOVIE, "mv_1", "Inception", null, ItemStatus.WATCHLIST, false, now, now),
        UserItem("u3", MediaType.BOOK, "bk_1", "1984", null, ItemStatus.COMPLETED, false, now, now),
        UserItem("u4", MediaType.SERIES, "tv_2", "Stranger Things", null, ItemStatus.COMPLETED, true, now, now),
        UserItem("u5", MediaType.MOVIE, "mv_2", "The Matrix", null, ItemStatus.ABANDONED, false, now, now),
        UserItem("u6", MediaType.BOOK, "bk_2", "Dune", null, ItemStatus.WATCHLIST, false, now, now),
    )
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        LibraryScreenContent(
            state = LibraryUiState(
                selectedStatus = ItemStatus.IN_PROGRESS,
                selectedMediaType = null,
                items = sampleItems,
                isLoading = false,
            ),
            onItemClick = {},
            onStatusSelected = {},
            onMediaTypeSelected = {},
        )
    }
}
