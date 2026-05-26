package com.mediatracker.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.stringResource
import com.mediatracker.R
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
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
