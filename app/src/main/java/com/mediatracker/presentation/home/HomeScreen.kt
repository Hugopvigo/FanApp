package com.mediatracker.presentation.home

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.displayLabel
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.FeaturedCardSkeleton
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.components.MediaCard
import com.mediatracker.presentation.components.MediaRowSkeleton
import com.mediatracker.presentation.components.ShimmerBox
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        state.isLoading -> HomeLoadingSkeleton()
        state.error != null && state.trending.isEmpty() -> EmptyState(stringResource(R.string.error_generic))
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.statusBarsPadding())

        HomeHeader()

        GreetingLine(userName = state.userName, currentStreak = state.currentStreak)

                Spacer(Modifier.height(20.dp))

                // Featured card — primer item de trending
                val featured = state.trending.firstOrNull()
                if (featured != null) {
                    FeaturedCard(item = featured, onClick = { onItemClick(featured) })
                    Spacer(Modifier.height(20.dp))
                }

                // Continuar viendo
                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(
                        title = stringResource(R.string.home_continue),
                        action = stringResource(R.string.home_see_all),
                    )
                    Spacer(Modifier.height(10.dp))
                    ContinueRow(items = state.continueWatching, onItemClick = onItemClick)
                    Spacer(Modifier.height(24.dp))
                }

                // Trending — el resto (sin el featured)
                val trendingRest = state.trending.drop(1)
                if (trendingRest.isNotEmpty()) {
                    SectionHeader(title = stringResource(R.string.home_trending))
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(trendingRest, key = { it.id }) { item ->
                            MediaCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

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
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                "${dayFormat.format(Date(cal.timeInMillis))} · ${cal.get(Calendar.DAY_OF_MONTH)} ${monthFormat.format(Date(cal.timeInMillis))}"
            }
            Text(
                text = today,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

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
private fun GreetingLine(userName: String?, currentStreak: Int = 0) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 6 -> "Buenas noches"
        hour < 12 -> "Buenos días"
        hour < 20 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val firstName = userName?.takeIf { it.isNotBlank() }?.split(" ")?.firstOrNull()
    val displayText = if (firstName != null) "$greeting, $firstName" else greeting
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = DisplayFontFamily,
                letterSpacing = (-0.4).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (currentStreak > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.fanAppColors.gradientAccent.first().copy(alpha = 0.15f),
            ) {
                Text(
                    text = "🔥 $currentStreak",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.fanAppColors.gradientAccent.first(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        } else {
            Text(text = "✨", fontSize = 22.sp)
        }
    }
}

// ─── Featured card ────────────────────────────────────────────────────────────
@Composable
private fun FeaturedCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fanColors = MaterialTheme.fanAppColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        if (item.posterUrl.isNotBlank()) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(fanColors.gradientAccent)),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    ),
                ),
        )

        if (item.rating > 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.48f),
            ) {
                Text(
                    text = "★ ${"%.1f".format(item.rating)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
            ) {
                Text(
                    text = "TRENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.mediaType.displayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.80f),
                )
                if (item.releaseDate.isNotBlank()) {
                    Text(
                        text = "· ${item.releaseDate.take(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.60f),
                    )
                }
            }
        }
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

// ─── Loading skeleton ─────────────────────────────────────────────────────────
@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(12.dp))
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(190.dp)
                .height(28.dp),
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(Modifier.height(16.dp))
        FeaturedCardSkeleton()
        Spacer(Modifier.height(20.dp))
        SectionHeaderSkeleton()
        MediaRowSkeleton()
        Spacer(Modifier.height(16.dp))
        SectionHeaderSkeleton()
        MediaRowSkeleton()
    }
}

@Composable
private fun SectionHeaderSkeleton() {
    ShimmerBox(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .width(110.dp)
            .height(18.dp),
        shape = RoundedCornerShape(4.dp),
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun HomeScreenContentPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        HomeScreenContent(
            state = HomeUiState(
                userName = "Hugo",
                trending = listOf(
                    MediaItem("t1", MediaType.SERIES, "Stranger Things", "", "https://image.tmdb.org/t/p/w500/x2LSRK2Cm7MZhjluni1msVJ3wDF.jpg", "2016", 8.7f, listOf("Fantasy")),
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
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun HomeLoadingSkeletonPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Dark) {
        HomeLoadingSkeleton()
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
