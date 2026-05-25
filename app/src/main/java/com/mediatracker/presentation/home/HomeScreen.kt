package com.mediatracker.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.displayLabel
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.FeaturedCardSkeleton
import com.mediatracker.presentation.components.MediaRow
import com.mediatracker.presentation.components.MediaRowSkeleton
import com.mediatracker.presentation.components.ShimmerBox
import com.mediatracker.presentation.theme.AppTheme
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
        state.isLoading -> HomeLoadingSkeleton()
        state.error != null && state.trending.isEmpty() -> EmptyState(stringResource(R.string.error_generic))
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                GreetingHeader(userName = state.userName)

                // Featured card — primer item de trending
                val featured = state.trending.firstOrNull()
                if (featured != null) {
                    FeaturedCard(item = featured, onClick = { onItemClick(featured) })
                    Spacer(Modifier.height(20.dp))
                }

                // Continuar viendo
                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(stringResource(R.string.home_continue))
                    MediaRow(items = state.continueWatching, onItemClick = onItemClick)
                    Spacer(Modifier.height(16.dp))
                }

                // Trending — el resto (sin el featured)
                val trendingRest = state.trending.drop(1)
                if (trendingRest.isNotEmpty()) {
                    SectionHeader(stringResource(R.string.home_trending))
                    MediaRow(items = trendingRest, onItemClick = onItemClick)
                    Spacer(Modifier.height(16.dp))
                }

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
private fun GreetingHeader(
    userName: String?,
    modifier: Modifier = Modifier,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 6..11  -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else      -> "Buenas noches"
    }
    val firstName = userName?.takeIf { it.isNotBlank() }?.split(" ")?.firstOrNull()

    Text(
        text = if (firstName != null) "$greeting, $firstName" else greeting,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

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

        // Scrim gradient: transparente arriba → oscuro abajo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    ),
                ),
        )

        // Rating badge — esquina superior derecha
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

        // Info superpuesta: badge TRENDING + título + meta
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

@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(12.dp))
        // Greeting skeleton
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 600)
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
