package com.mediatracker.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.MediaTrackerTheme

private val POSTER_GRADIENTS = listOf(
    listOf(Color(0xFFFFB3D1), Color(0xFFF06292)),
    listOf(Color(0xFFFCC1D4), Color(0xFFC875B0)),
    listOf(Color(0xFFC9B1EF), Color(0xFF7C4DFF)),
    listOf(Color(0xFFB39DDB), Color(0xFF5E35B1)),
    listOf(Color(0xFFFFD5A6), Color(0xFFEF7C80)),
    listOf(Color(0xFFFFC1B3), Color(0xFFD65F8E)),
    listOf(Color(0xFFA4C7F0), Color(0xFF7C5FDB)),
    listOf(Color(0xFF7BAEF0), Color(0xFF3A4DBB)),
    listOf(Color(0xFFA9E0C1), Color(0xFF4A9D7A)),
    listOf(Color(0xFFF4A3A3), Color(0xFFA04060)),
    listOf(Color(0xFF5A4080), Color(0xFF1F1438)),
    listOf(Color(0xFFC1F0D4), Color(0xFF5DC88E)),
)

private fun titleGradientIndex(title: String): Int {
    var h = 0
    for (c in title) h = (h * 31 + c.code) and 0xFFFF
    return h % POSTER_GRADIENTS.size
}

@Composable
fun GradientPoster(
    title: String,
    kind: MediaType,
    modifier: Modifier = Modifier,
    rating: Float = 0f,
) {
    val idx = titleGradientIndex(title)
    val colors = POSTER_GRADIENTS[idx]
    val brush = Brush.linearGradient(colors, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY))
    val shape = RoundedCornerShape(14.dp)
    val isBook = kind == MediaType.BOOK

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    ) {
        // Radial top highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                        radius = Float.POSITIVE_INFINITY,
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                    ),
                ),
        )
        // Vertical bottom fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
                    ),
                ),
        )
        // Book spine shadow
        if (isBook) {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 9999.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent),
                        ),
                    ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (isBook) 12.dp else 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = DisplayFontFamily,
                    color = Color.White.copy(alpha = 0.97f),
                    lineHeight = 15.sp,
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = when (kind) {
                        MediaType.SERIES -> "🎬"
                        MediaType.MOVIE  -> "🎥"
                        MediaType.BOOK   -> "📖"
                    },
                    fontSize = 12.sp,
                )
                if (rating > 0f) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.32f),
                    ) {
                        Text(
                            text = "★ ${"%.1f".format(rating)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavBadge: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardPressScale",
    )

    Column(
        modifier = modifier
            .width(120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box {
            // fillMaxWidth + ratio 2:3: en LazyRow mide 120x180; en rejillas con
            // weight llena la celda en cualquier tamaño de pantalla
            if (item.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientPoster(
                    title = item.title,
                    kind = item.mediaType,
                    rating = item.rating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                )
            }
            // Fav sparkle badge
            if (showFavBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⭐", fontSize = 11.sp)
                }
            }
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when (item.mediaType) {
                    MediaType.SERIES -> "🎬"
                    MediaType.MOVIE  -> "🎥"
                    MediaType.BOOK   -> "📖"
                },
                fontSize = 10.sp,
            )
            if (item.rating > 0f) {
                Text(
                    text = "★ ${"%.1f".format(item.rating)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaCardPreview() {
    MediaTrackerTheme(appTheme = AppTheme.Fantasy) {
        MediaCard(
            item = MediaItem(
                id = "preview_1",
                mediaType = MediaType.MOVIE,
                title = "Interestellar",
                overview = "Un viaje interestelar para salvar la humanidad.",
                posterUrl = "",
                releaseDate = "2014",
                rating = 4.8f,
                genres = listOf("Sci-Fi", "Drama"),
            ),
            onClick = {},
        )
    }
}
