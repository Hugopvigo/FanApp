package com.mediatracker.presentation.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
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
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors

private val HERO_HEIGHT = 380.dp

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> DetailScreenSkeleton()
        state.error != null -> ErrorState(state.error ?: stringResource(R.string.error_unknown))
        state.item != null -> DetailContent(
            state = state,
            onBack = onBack,
            onStatusSelected = viewModel::onStatusSelected,
            onRemoveFromList = viewModel::onRemoveFromList,
            onToggleFavorite = viewModel::onToggleFavorite,
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onStatusSelected: (ItemStatus) -> Unit,
    onRemoveFromList: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val item = state.item ?: return
    val userItem = state.userItem
    val bgColor = MaterialTheme.colorScheme.background
    val fanColors = MaterialTheme.fanAppColors
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Scrollable content ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // ── Hero ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_HEIGHT),
            ) {
                // Poster o gradient fallback
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

                // Scrim: sutil arriba → opaco abajo (hacia bgColor)
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

                // Info superpuesta: badges + título + géneros
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

            // ── Contenido bajo el hero ────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Status chips + Remove
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

                // Favorite toggle
                if (userItem != null) {
                    FavoriteToggle(
                        isFavorite = userItem.favorite,
                        onToggle = onToggleFavorite,
                        enabled = userItem.status != ItemStatus.ABANDONED,
                    )
                }

                // Sinopsis
                if (item.overview.isNotBlank()) {
                    Text(
                        text = item.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Datos extra (temporadas, duración, autor, etc.)
                item.extraData?.let { ExtraInfo(extra = it, mediaType = item.mediaType) }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Back button flotante ──────────────────────────────────────────
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
                ),
                isLoading = false,
            ),
            onBack = {},
            onStatusSelected = {},
            onRemoveFromList = {},
            onToggleFavorite = {},
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
