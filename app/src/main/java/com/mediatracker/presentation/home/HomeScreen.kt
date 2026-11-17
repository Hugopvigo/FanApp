package com.mediatracker.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.EmptyState
import com.mediatracker.presentation.components.MediaCard
import com.mediatracker.presentation.home.components.ContinueRow
import com.mediatracker.presentation.home.components.FeaturedCard
import com.mediatracker.presentation.home.components.GreetingLine
import com.mediatracker.presentation.home.components.HomeHeader
import com.mediatracker.presentation.home.components.HomeLoadingSkeleton
import com.mediatracker.presentation.home.components.SectionHeader
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun HomeScreen(
    onItemClick: (MediaItem) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationPermissionAsked by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { notificationPermissionAsked = true }

    LaunchedEffect(Unit) {
        if (notificationPermissionAsked) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionAsked = true
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationPermissionAsked = true
            }
        }
    }

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

                val featured = state.trending.firstOrNull()
                if (featured != null) {
                    FeaturedCard(item = featured, onClick = { onItemClick(featured) })
                    Spacer(Modifier.height(20.dp))
                }

                if (state.continueWatching.isNotEmpty()) {
                    SectionHeader(
                        title = stringResource(R.string.home_continue),
                        action = stringResource(R.string.home_see_all),
                    )
                    Spacer(Modifier.height(10.dp))
                    ContinueRow(items = state.continueWatching, onItemClick = onItemClick)
                    Spacer(Modifier.height(24.dp))
                }

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
