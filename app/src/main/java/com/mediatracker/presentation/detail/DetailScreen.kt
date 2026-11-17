package com.mediatracker.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.displayLabel
import com.mediatracker.presentation.components.DetailScreenSkeleton
import com.mediatracker.presentation.components.ErrorState
import com.mediatracker.presentation.components.FavoriteToggle
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.StatusChip
import com.mediatracker.presentation.detail.components.ExtraInfo
import com.mediatracker.presentation.detail.components.GoToPageDialog
import com.mediatracker.presentation.detail.components.HeroBadge
import com.mediatracker.presentation.detail.components.NotesField
import com.mediatracker.presentation.detail.components.PageProgressStepper
import com.mediatracker.presentation.detail.components.SeasonEpisodeStepper
import com.mediatracker.presentation.detail.components.StarRatingBar
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors

private val HERO_HEIGHT = 380.dp

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onNavigateToFanCard: ((apiId: String, mediaType: MediaType, rating: Int?) -> Unit)? = null,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fanColors = MaterialTheme.fanAppColors
    var showRatingCardPrompt by rememberSaveable { mutableStateOf(false) }
    val previousRating = rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.userItem?.userRating) {
        val currentRating = state.userItem?.userRating
        if (currentRating != null && currentRating != previousRating.value && previousRating.value != null) {
            showRatingCardPrompt = true
        }
        previousRating.value = currentRating
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> DetailScreenSkeleton()
            state.error != null -> ErrorState(state.error ?: stringResource(R.string.error_unknown))
            state.item != null -> DetailContent(
                state = state,
                onBack = onBack,
                onStatusSelected = viewModel::onStatusSelected,
                onRemoveFromList = viewModel::onRemoveFromList,
                onToggleFavorite = viewModel::onToggleFavorite,
                onRatingChanged = viewModel::onRatingChanged,
                onNotesChanged = viewModel::onNotesChanged,
                onSeasonEpisodeChanged = viewModel::onSeasonEpisodeChanged,
                onPageProgressChanged = viewModel::onPageProgressChanged,
                onEpisodeToggle = viewModel::onEpisodeToggle,
                onOpenGoToPageDialog = viewModel::onOpenGoToPageDialog,
            )
            else -> DetailScreenSkeleton()
        }

    if (state.showGoToPageDialog) {
        GoToPageDialog(
            onConfirm = viewModel::onGoToPageConfirm,
            onDismiss = viewModel::onDismissGoToPageDialog,
        )
    }

    if (showRatingCardPrompt && onNavigateToFanCard != null) {
        Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            action = {
                TextButton(onClick = {
                    showRatingCardPrompt = false
                    val userItem = state.userItem
                    if (userItem != null) {
                        onNavigateToFanCard(userItem.apiId, userItem.mediaType, userItem.userRating)
                    }
                }) { Text(stringResource(R.string.fancard_share), color = fanColors.gradientAccent.first()) }
            },
            dismissAction = {
                IconButton(onClick = { showRatingCardPrompt = false }) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            },
        ) { Text(stringResource(R.string.fancard_rating_prompt)) }
    }

    if (state.suggestBookComplete) {
        Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            action = {
                TextButton(onClick = viewModel::onConfirmBookComplete) {
                    Text(stringResource(R.string.detail_action_complete), color = fanColors.gradientAccent.first())
                }
            },
            dismissAction = {
                IconButton(onClick = viewModel::onDismissBookComplete) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            },
        ) { Text(stringResource(R.string.detail_book_complete_prompt)) }
    }

    if (state.suggestSeasonAdvance) {
        Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            action = {
                TextButton(onClick = viewModel::onConfirmSeasonAdvance) {
                    Text(stringResource(R.string.detail_action_advance), color = fanColors.gradientAccent.first())
                }
            },
            dismissAction = {
                IconButton(onClick = viewModel::onDismissSeasonAdvance) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            },
        ) { Text(stringResource(R.string.detail_season_advance_prompt)) }
    }

    if (state.suggestSeriesComplete) {
        Snackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            action = {
                TextButton(onClick = viewModel::onConfirmSeriesComplete) {
                    Text(stringResource(R.string.detail_action_complete), color = fanColors.gradientAccent.first())
                }
            },
            dismissAction = {
                IconButton(onClick = viewModel::onDismissSeriesComplete) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            },
        ) { Text(stringResource(R.string.detail_series_complete_prompt)) }
    }
}
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onStatusSelected: (ItemStatus) -> Unit,
    onRemoveFromList: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRatingChanged: (Int?) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSeasonEpisodeChanged: (Int?, Int?) -> Unit,
    onPageProgressChanged: (Int?, Int?) -> Unit,
    onEpisodeToggle: (Int, Int) -> Unit,
    onOpenGoToPageDialog: () -> Unit,
) {
    val item = state.item ?: return
    val userItem = state.userItem
    val bgColor = MaterialTheme.colorScheme.background
    val fanColors = MaterialTheme.fanAppColors
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_HEIGHT),
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
                                colorStops = arrayOf(
                                    0.00f to Color.Black.copy(alpha = 0.18f),
                                    0.38f to Color.Transparent,
                                    0.68f to bgColor.copy(alpha = 0.50f),
                                    1.00f to bgColor,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HeroBadge(item.mediaType.displayLabel)
                        if (item.releaseDate.isNotBlank()) {
                            HeroBadge(item.releaseDate.take(4))
                        }
                        if (item.rating > 0f) {
                            HeroBadge("★ ${"%.1f".format(item.rating)}")
                        }
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.take(3).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ItemStatus.entries.forEach { status ->
                        StatusChip(
                            status = status,
                            selected = userItem?.status == status,
                            onClick = { onStatusSelected(status) },
                        )
                    }
                    if (userItem != null) {
                        OutlinedButton(onClick = onRemoveFromList) {
                            Text(stringResource(R.string.detail_remove))
                        }
                    }
                }

                if (userItem != null) {
                    GlassSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            FavoriteToggle(
                                isFavorite = userItem.favorite,
                                onToggle = onToggleFavorite,
                                enabled = userItem.status != ItemStatus.ABANDONED,
                            )
                            StarRatingBar(
                                rating = userItem.userRating,
                                onRatingChanged = onRatingChanged,
                            )
                            NotesField(
                                notes = userItem.notes.orEmpty(),
                                onNotesChanged = onNotesChanged,
                            )
                        }
                    }
                }

                if (userItem != null && item.mediaType == MediaType.SERIES && userItem.status == ItemStatus.IN_PROGRESS) {
                    GlassSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            SeasonEpisodeStepper(
                                season = userItem.currentSeason,
                                episode = userItem.currentEpisode,
                                onChanged = onSeasonEpisodeChanged,
                            )

                            val currentSeason = userItem.currentSeason ?: 1
                            val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
                            val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
                            val epsPerSeason = state.seasonEpisodeCount
                                ?: if (numberOfEpisodes != null && numberOfSeasons != null && numberOfSeasons > 0) {
                                    numberOfEpisodes / numberOfSeasons
                                } else null
                            val seasonWatched = userItem.watchedEpisodes[currentSeason] ?: emptyList()

                            if (epsPerSeason != null && epsPerSeason > 0) {
                                EpisodeTracker(
                                    season = currentSeason,
                                    numberOfEpisodes = epsPerSeason,
                                    watchedEpisodes = seasonWatched,
                                    onEpisodeToggle = { ep -> onEpisodeToggle(currentSeason, ep) },
                                )
                            }
                        }
                    }
                }

                if (userItem != null && item.mediaType == MediaType.BOOK && userItem.status == ItemStatus.IN_PROGRESS) {
                    GlassSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            PageProgressStepper(
                                currentPage = userItem.currentPage,
                                totalPages = userItem.totalPages,
                                apiPageCount = item.extraData?.get("pageCount")?.toIntOrNull(),
                                onChanged = onPageProgressChanged,
                                onGoToPageClick = onOpenGoToPageDialog,
                            )
                        }
                    }
                }

                if (item.overview.isNotBlank() || item.extraData != null) {
                    GlassSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (item.overview.isNotBlank()) {
                                Text(
                                    text = item.overview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            item.extraData?.let { ExtraInfo(extra = it, mediaType = item.mediaType) }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.38f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}


@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DetailContentPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        DetailContent(
            state = DetailUiState(
                item = MediaItem(
                    id = "preview_d1",
                    mediaType = MediaType.MOVIE,
                    title = "Inception",
                    overview = "Un ladrón especializado en extraer secretos del subconsciente a través de los sueños recibe la tarea de implantar una idea en la mente de un CEO.",
                    posterUrl = "",
                    releaseDate = "2010-07-16",
                    rating = 8.8f,
                    genres = listOf("Acción", "Sci-Fi", "Thriller"),
                    extraData = mapOf("runtime" to "148", "creators" to "Christopher Nolan"),
                ),
                userItem = UserItem(
                    id = "preview_u1",
                    mediaType = MediaType.MOVIE,
                    apiId = "mv_1",
                    title = "Inception",
                    posterUrl = null,
                    status = ItemStatus.COMPLETED,
                    favorite = true,
                    addedAt = 0L,
                    updatedAt = 0L,
                    userRating = 4,
                    notes = "Amazing movie!",
                ),
                isLoading = false,
            ),
            onBack = {},
            onStatusSelected = {},
            onRemoveFromList = {},
            onToggleFavorite = {},
            onRatingChanged = {},
            onNotesChanged = {},
    onSeasonEpisodeChanged = { _, _ -> },
    onPageProgressChanged = { _, _ -> },
    onEpisodeToggle = { _, _ -> },
    onOpenGoToPageDialog = {},
)
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun DetailSkeletonPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Dark) {
        DetailScreenSkeleton()
    }
}
