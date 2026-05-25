package com.mediatracker.presentation.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.MediaCard
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors
import java.util.Calendar

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
                Spacer(Modifier.statusBarsPadding())

                HomeHeader()

                GreetingLine()

                Spacer(Modifier.height(20.dp))

                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(
                        title = stringResource(R.string.home_continue),
                        action = stringResource(R.string.home_see_all),
                    )
                    Spacer(Modifier.height(10.dp))
                    ContinueRow(items = state.continueWatching, onItemClick = onItemClick)
                    Spacer(Modifier.height(24.dp))
                }

                SectionHeader(title = stringResource(R.string.home_trending))
                Spacer(Modifier.height(10.dp))
                if (state.trending.isEmpty()) {
                    EmptyState(stringResource(R.string.empty_list))
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.trending, key = { it.id }) { item ->
                            MediaCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                if (state.favorites.isNotEmpty()) {
                    SectionHeader(
                        title = stringResource(R.string.home_favorites),
                        action = stringResource(R.string.home_see_all),
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.favorites, key = { it.id }) { item ->
                            MediaCard(item = item, onClick = { onItemClick(item) }, showFavBadge = true)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                if (state.recent.isNotEmpty()) {
                    SectionHeader(title = stringResource(R.string.home_recent))
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.recent, key = { it.id }) { item ->
                            MediaCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader() {
    val fanColors = MaterialTheme.fanAppColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // App logo squircle
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(fanColors.gradientAccent)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "FanApp",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    letterSpacing = (-0.3).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            val today = remember {
                val cal = Calendar.getInstance()
                val days = listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb")
                val months = listOf("ene", "feb", "mar", "abr", "may", "jun",
                    "jul", "ago", "sep", "oct", "nov", "dic")
                "${days[cal.get(Calendar.DAY_OF_WEEK) - 1]} · ${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]}"
            }
            Text(
                text = today,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Avatar placeholder (initial)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(fanColors.gradient1)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✨",
                fontSize = 18.sp,
            )
        }
    }
}

// ─── Greeting ─────────────────────────────────────────────────────────────────
@Composable
private fun GreetingLine() {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 6  -> "Buenas noches"
        hour < 12 -> "Buenos días"
        hour < 20 -> "Buenas tardes"
        else      -> "Buenas noches"
    }
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = DisplayFontFamily,
                letterSpacing = (-0.4).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(text = "✨", fontSize = 22.sp)
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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

// ─── "Continue watching" horizontal glass cards ───────────────────────────────
@Composable
private fun ContinueRow(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { item ->
            ContinueCard(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
private fun ContinueCard(
    item: MediaItem,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        radius = 18.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Poster thumbnail
            if (item.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(width = 54.dp, height = 80.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientPoster(
                    title = item.title,
                    kind = item.mediaType,
                    modifier = Modifier.size(width = 54.dp, height = 80.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when (item.mediaType) {
                        MediaType.SERIES -> "🎬 En progreso"
                        MediaType.MOVIE  -> "🎥 En progreso"
                        MediaType.BOOK   -> "📖 Leyendo"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.rating > 0f) {
                    Text(
                        text = "★ ${"%.1f".format(item.rating)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, heightDp = 700)
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
                    MediaItem("c2", MediaType.BOOK, "Dune", "", "", "1965", 4.3f, listOf("Sci-Fi")),
                ),
                favorites = listOf(
                    MediaItem("f1", MediaType.MOVIE, "The Matrix", "", "", "1999", 8.7f, listOf("Sci-Fi")),
                ),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Dark) {
        HomeScreenContent(
            state = HomeUiState(
                trending = listOf(
                    MediaItem("t1", MediaType.MOVIE, "Dune Part Two", "", "", "2024", 8.5f, listOf("Sci-Fi")),
                ),
                isLoading = false,
            ),
        )
    }
}
