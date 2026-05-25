package com.mediatracker.presentation.discover

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.ErrorState
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.MediaCard
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors
import kotlinx.coroutines.delay

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.statusBarsPadding())

        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily,
                    letterSpacing = (-0.4).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(text = "✨", fontSize = 18.sp)
        }

        // ── Glass search bar ─────────────────────────────────────────────────
        GlassSearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        // ── Category chips ───────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                GlassChip(
                    text = "$icon $label",
                    active = state.selectedTab == type,
                    onClick = { onTabSelected(type) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> LoadingState()
            state.error != null && state.searchResults.isEmpty() ->
                ErrorState(state.error ?: stringResource(R.string.error_unknown))
            state.searchQuery.isNotBlank() && state.searchResults.isEmpty() ->
                EmptyState(stringResource(R.string.no_results))
            state.searchQuery.isBlank() -> {
                // ── Trending top section ──────────────────────────────────────
                SectionRow(
                    title = stringResource(R.string.discover_top_trending),
                    action = "Ver más",
                )
                Spacer(Modifier.height(10.dp))
                when {
                    state.isLoadingTrending -> LoadingState()
                    state.error != null -> ErrorState(state.error ?: stringResource(R.string.error_unknown))
                    state.trending.isEmpty() -> EmptyState(stringResource(R.string.discover_no_trending))
                    else -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(state.trending.take(10).mapIndexed { i, item -> i + 1 to item }) { (rank, item) ->
                                TrendingCard(rank = rank, item = item, onClick = { onItemClick(item) })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            else -> {
                // ── Search results grid ───────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.searchResults.chunked(3).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { item ->
                                MediaCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Glass search bar ─────────────────────────────────────────────────────────
@Composable
private fun GlassSearchBar(
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

    val fanColors = MaterialTheme.fanAppColors
    val shape = RoundedCornerShape(14.dp)

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
                modifier = Modifier.width(18.dp),
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
                            text = stringResource(R.string.discover_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
            if (localQuery.isNotEmpty()) {
                Text(
                    text = "✕",
                    modifier = Modifier.clickable { localQuery = "" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Glass pill chip ──────────────────────────────────────────────────────────
@Composable
private fun GlassChip(
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
            .border(
                width = 1.dp,
                color = if (active) Color.Transparent else fanColors.borderColor,
                shape = shape,
            )
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

// ─── Trending card with hollow rank number ────────────────────────────────────
@Composable
private fun TrendingCard(
    rank: Int,
    item: MediaItem,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (item.posterUrl.isNotBlank()) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .padding(start = 28.dp)
                    .width(110.dp)
                    .height(165.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            GradientPoster(
                title = item.title,
                kind = item.mediaType,
                rating = item.rating,
                modifier = Modifier
                    .padding(start = 28.dp)
                    .width(110.dp)
                    .height(165.dp),
            )
        }
        // Hollow outline rank number
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = DisplayFontFamily,
                letterSpacing = (-2).sp,
            ),
            fontWeight = FontWeight.Normal,
            color = primary.copy(alpha = 0.80f),
            modifier = Modifier.offset(x = (-4).dp, y = 8.dp),
        )
    }
}

// ─── Section row header ───────────────────────────────────────────────────────
@Composable
private fun SectionRow(title: String, action: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = DisplayFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.3).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DiscoverScreenPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        DiscoverScreenContent(
            state = DiscoverUiState(
                selectedTab = MediaType.SERIES,
                trending = listOf(
                    MediaItem("t1", MediaType.SERIES, "Stranger Things", "", "", "2016", 8.7f, listOf("Fantasy")),
                    MediaItem("t2", MediaType.MOVIE, "Inception", "", "", "2010", 8.8f, listOf("Sci-Fi")),
                    MediaItem("t3", MediaType.BOOK, "Dune", "", "", "1965", 4.3f, listOf("Sci-Fi")),
                ),
                isLoadingTrending = false,
            ),
            onItemClick = {},
            onSearchQueryChange = {},
            onTabSelected = {},
        )
    }
}
