package com.mediatracker.presentation.quickadd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatracker.R
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItemWithUserStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.displayLabel
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.components.ShimmerBox
import com.mediatracker.presentation.components.statusIcon
import com.mediatracker.presentation.components.fanColor
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onItemClick: (MediaItemWithUserStatus) -> Unit = {},
    viewModel: QuickAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.justAddedId) {
        if (state.justAddedId != null) {
            delay(800)
            viewModel.clearJustAdded()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
        ) {
            Text(
                text = stringResource(R.string.quick_add_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = DisplayFontFamily,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(8.dp))

            QuickAddSearchBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            MediaTypeChips(
                selected = state.selectedTab,
                onSelected = viewModel::onTabSelected,
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> QuickAddLoadingSkeleton()
                state.error != null -> {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(20.dp),
                    )
                }
                state.results.isEmpty() && state.query.isNotBlank() -> {
                    Text(
                        text = stringResource(R.string.no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.results, key = { it.media.id }) { item ->
                            QuickAddResultRow(
                                item = item,
                                justAdded = state.justAddedId == item.media.id,
                                onAdd = { viewModel.onAddToWatchlist(item.media) },
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAddSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localQuery by rememberSaveable { mutableStateOf(query) }
    LaunchedEffect(query) { if (query != localQuery) localQuery = query }
    LaunchedEffect(localQuery) {
        delay(300)
        if (localQuery != query) onQueryChange(localQuery)
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        radius = 14.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            BasicTextField(
                value = localQuery,
                onValueChange = { localQuery = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (localQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.quick_add_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun MediaTypeChips(
    selected: MediaType,
    onSelected: (MediaType) -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val shape = RoundedCornerShape(999.dp)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(MediaType.entries) { type ->
            val label = when (type) {
                MediaType.SERIES -> stringResource(R.string.discover_series)
                MediaType.MOVIE -> stringResource(R.string.discover_movies)
                MediaType.BOOK -> stringResource(R.string.discover_books)
            }
            val icon = when (type) {
                MediaType.SERIES -> "🎬"
                MediaType.MOVIE -> "🎥"
                MediaType.BOOK -> "📖"
            }
            val isActive = selected == type
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (isActive) Brush.linearGradient(fanColors.gradientAccent)
                        else Brush.linearGradient(listOf(fanColors.surfaceGlass, fanColors.surfaceGlass)),
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) Color.Transparent else fanColors.borderColor,
                        shape = shape,
                    )
                    .clickable { onSelected(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$icon $label",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun QuickAddResultRow(
    item: MediaItemWithUserStatus,
    justAdded: Boolean,
    onAdd: () -> Unit,
    onClick: () -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val media = item.media
    val existingStatus = item.userStatus?.status

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = fanColors.surfaceGlass.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = existingStatus != null) { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (media.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = media.posterUrl,
                    contentDescription = media.title,
                    modifier = Modifier
                        .width(52.dp)
                        .height(78.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientPoster(
                    title = media.title,
                    kind = media.mediaType,
                    rating = media.rating,
                    modifier = Modifier
                        .width(52.dp)
                        .height(78.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = media.mediaType.displayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (media.releaseDate.isNotBlank()) {
                        Text(
                            text = "· ${media.releaseDate.take(4)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (existingStatus != null) {
                    val statusLabel = when (existingStatus) {
                        ItemStatus.WATCHLIST -> stringResource(R.string.quick_add_status_watchlist)
                        ItemStatus.IN_PROGRESS -> stringResource(R.string.quick_add_status_in_progress)
                        ItemStatus.COMPLETED -> stringResource(R.string.quick_add_status_completed)
                        ItemStatus.ABANDONED -> stringResource(R.string.quick_add_status_abandoned)
                    }
                    Text(
                        text = "${existingStatus.statusIcon} $statusLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = existingStatus.fanColor(fanColors),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (existingStatus != null || justAdded) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.quick_add_added),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onAdd),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.quick_add_add),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddLoadingSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(5) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(52.dp)
                        .height(78.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(10.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }
    }
}
