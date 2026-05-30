package com.mediatracker.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.stringResource
import com.mediatracker.R
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.theme.fanAppColors

@Composable
fun LibraryItemCard(
    userItem: UserItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            if (!userItem.posterUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = userItem.posterUrl,
                    contentDescription = userItem.title,
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                    error = {
                        GradientPoster(
                            title = userItem.title,
                            kind = userItem.mediaType,
                            modifier = Modifier.size(width = 120.dp, height = 180.dp),
                        )
                    },
                )
            } else {
                GradientPoster(
                    title = userItem.title,
                    kind = userItem.mediaType,
                    modifier = Modifier.size(width = 120.dp, height = 180.dp),
                )
            }

        if (userItem.mediaType == MediaType.SERIES &&
            userItem.status == ItemStatus.IN_PROGRESS &&
            userItem.currentSeason != null &&
            userItem.currentEpisode != null
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            ) {
                Text(
                    text = "T${userItem.currentSeason}E${userItem.currentEpisode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }

        if (userItem.mediaType == MediaType.BOOK &&
            userItem.status == ItemStatus.IN_PROGRESS &&
            userItem.currentPage != null &&
            userItem.currentPage > 0
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.68f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            ) {
                Text(
                    text = if (userItem.totalPages != null && userItem.totalPages > 0)
                        "${userItem.currentPage}/${userItem.totalPages}"
                    else
                        "${userItem.currentPage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }

        if (userItem.status == ItemStatus.IN_PROGRESS) {
            val progress = when (userItem.mediaType) {
                MediaType.BOOK -> {
                    val cp = userItem.currentPage ?: 0
                    val tp = userItem.totalPages
                    if (tp != null && tp > 0 && cp > 0) cp.toFloat() / tp.toFloat() else 0f
                }
                MediaType.SERIES -> {
                    val watchedCount = userItem.watchedEpisodes.values.sumOf { it.size }
                    if (watchedCount > 0) {
                        val maxSeason = userItem.watchedEpisodes.keys.maxOrNull()
                            ?: userItem.currentSeason ?: 1
                        val maxEp = userItem.watchedEpisodes.values.maxOfOrNull { it.maxOrNull() ?: 0 }
                            ?: userItem.currentEpisode ?: 0
                        val totalEstimate = (maxSeason * maxOf(maxEp, 10)).coerceAtLeast(watchedCount)
                        watchedCount.toFloat() / totalEstimate
                    } else {
                        val season = userItem.currentSeason ?: 0
                        val episode = userItem.currentEpisode ?: 0
                        if (season > 0 && episode > 0) {
                            val totalEstimate = season * 10
                            ((season - 1) * 10 + episode).toFloat() / totalEstimate
                        } else {
                            0f
                        }
                    }
                }
                else -> 0f
            }
        if (progress > 0f) {
            val fanColors = MaterialTheme.fanAppColors
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)),
                color = fanColors.gradientAccent.first(),
                trackColor = Color.Transparent,
            )
        }
    }
}

    Text(
            text = userItem.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = when (userItem.mediaType) {
                    MediaType.SERIES -> stringResource(R.string.media_type_series)
                    MediaType.MOVIE -> stringResource(R.string.media_type_movie)
                    MediaType.BOOK -> stringResource(R.string.media_type_book)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (userItem.favorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorito",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        if (userItem.userRating != null) {
            val fanColors = MaterialTheme.fanAppColors
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= userItem.userRating) Icons.Filled.Star
                        else Icons.Filled.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (star <= userItem.userRating) fanColors.gradientAccent.first()
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f),
                    )
                }
            }
        }
    }
}
