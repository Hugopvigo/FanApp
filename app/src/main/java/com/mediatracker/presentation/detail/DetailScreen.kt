package com.mediatracker.presentation.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.mediatracker.presentation.components.StatusChip
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
    var showRatingCardPrompt by remember { mutableStateOf(false) }
    val previousRating = remember { mutableStateOf<Int?>(null) }

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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                    FavoriteToggle(
                        isFavorite = userItem.favorite,
                        onToggle = onToggleFavorite,
                        enabled = userItem.status != ItemStatus.ABANDONED,
                    )
                }

                if (userItem != null) {
                    StarRatingBar(
                        rating = userItem.userRating,
                        onRatingChanged = onRatingChanged,
                    )
                }

                if (userItem != null) {
                    NotesField(
                        notes = userItem.notes.orEmpty(),
                        onNotesChanged = onNotesChanged,
                    )
                }

            if (userItem != null && item.mediaType == MediaType.SERIES && userItem.status == ItemStatus.IN_PROGRESS) {
                SeasonEpisodeStepper(
                    season = userItem.currentSeason,
                    episode = userItem.currentEpisode,
                    onChanged = onSeasonEpisodeChanged,
                )

                val currentSeason = userItem.currentSeason ?: 1
                val numberOfEpisodes = item.extraData?.get("numberOfEpisodes")?.toIntOrNull()
                val numberOfSeasons = item.extraData?.get("numberOfSeasons")?.toIntOrNull()
                val epsPerSeason = if (numberOfEpisodes != null && numberOfSeasons != null && numberOfSeasons > 0) {
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

            if (userItem != null && item.mediaType == MediaType.BOOK && userItem.status == ItemStatus.IN_PROGRESS) {
                PageProgressStepper(
                    currentPage = userItem.currentPage,
                    totalPages = userItem.totalPages,
                    apiPageCount = item.extraData?.get("pageCount")?.toIntOrNull(),
                    onChanged = onPageProgressChanged,
                )
            }

                if (item.overview.isNotBlank()) {
                    Text(
                        text = item.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                item.extraData?.let { ExtraInfo(extra = it, mediaType = item.mediaType) }

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

@Composable
private fun HeroBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.40f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun ExtraInfo(extra: Map<String, String>, mediaType: MediaType) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        extra.forEach { (key, value) ->
            val label = when (key) {
                "numberOfSeasons" -> stringResource(R.string.detail_seasons)
                "numberOfEpisodes" -> stringResource(R.string.detail_episodes)
                "creators" -> stringResource(R.string.detail_creators)
                "runtime" -> stringResource(R.string.detail_runtime)
                "authors" -> stringResource(R.string.detail_author)
                "publisher" -> stringResource(R.string.detail_publisher)
                "pageCount" -> stringResource(R.string.detail_pages)
                else -> key
            }
            val displayValue = if (key == "runtime") "$value min" else value
            Row {
                Text(
                    text = "$label: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = displayValue, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StarRatingBar(
    rating: Int?,
    onRatingChanged: (Int?) -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    Column {
        Text(
            text = stringResource(R.string.detail_your_rating),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { star ->
                val selected = rating != null && star <= rating
                Icon(
                    imageVector = if (selected) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$star",
                    tint = if (selected) fanColors.gradientAccent.first()
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            onRatingChanged(if (rating == star) null else star)
                        },
                )
            }
        }
    }
}

@Composable
private fun NotesField(
    notes: String,
    onNotesChanged: (String) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.detail_notes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            placeholder = { Text(stringResource(R.string.detail_notes_hint)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun SeasonEpisodeStepper(
    season: Int?,
    episode: Int?,
    onChanged: (Int?, Int?) -> Unit,
) {
    val s = season ?: 1
    val e = episode ?: 1

    Column {
        Text(
            text = stringResource(R.string.detail_tracking),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepperControl(
                label = stringResource(R.string.detail_season),
                value = s,
                onDecrement = { onChanged(maxOf(1, s - 1), e) },
                onIncrement = { onChanged(s + 1, 1) },
                modifier = Modifier.weight(1f),
            )
            StepperControl(
                label = stringResource(R.string.detail_episode),
                value = e,
                onDecrement = { onChanged(s, maxOf(1, e - 1)) },
                onIncrement = { onChanged(s, e + 1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PageProgressStepper(
    currentPage: Int?,
    totalPages: Int?,
    apiPageCount: Int?,
    onChanged: (Int?, Int?) -> Unit,
) {
    val t = totalPages ?: apiPageCount ?: 0
    val c = currentPage ?: if (t > 0) 0 else 0
    val fanColors = MaterialTheme.fanAppColors

    Column {
        Text(
            text = stringResource(R.string.detail_page_tracking),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepperControl(
                label = stringResource(R.string.detail_current_page),
                value = c,
                onDecrement = { onChanged(maxOf(0, c - 1), t) },
                onIncrement = { onChanged(minOf(if (t > 0) t else c + 1, c + 1), t) },
                modifier = Modifier.weight(1f),
                allowZero = true,
            )
            StepperControl(
                label = stringResource(R.string.detail_total_pages),
                value = t,
                onDecrement = { onChanged(minOf(c, maxOf(1, t - 1)), maxOf(1, t - 1)) },
                onIncrement = { onChanged(c, t + 1) },
                modifier = Modifier.weight(1f),
                allowZero = true,
            )
        }
        if (t > 0 && c > 0) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { c.toFloat() / t.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = fanColors.gradientAccent.first(),
                trackColor = fanColors.surfaceAlpha.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(c.toFloat() / t.toFloat() * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StepperControl(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    allowZero: Boolean = false,
) {
    val fanColors = MaterialTheme.fanAppColors
    val minValue = if (allowZero) 0 else 1

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = fanColors.surfaceAlpha.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, fanColors.borderColor),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onDecrement, enabled = value > minValue) {
                    Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = fanColors.gradientAccent.first(),
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(18.dp))
                }
            }
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
