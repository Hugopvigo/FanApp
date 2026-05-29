package com.mediatracker.presentation.fancard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatracker.R
import com.mediatracker.domain.model.UserItem
import com.mediatracker.presentation.components.GradientPoster
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.FanAppColors
import com.mediatracker.presentation.theme.fanAppColors
import kotlinx.coroutines.launch

@Composable
fun FanCardScreen(
    onBack: () -> Unit,
    initialType: FanCardType = FanCardType.PROFILE,
    viewModel: FanCardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fanColors = MaterialTheme.fanAppColors
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedType by remember(initialType) { mutableStateOf(initialType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = stringResource(R.string.fancard_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    scope.launch {
                        FanCardShareHelper.shareBitmap(context, graphicsLayer)
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.fancard_share),
                    tint = fanColors.gradientAccent.first(),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FanCardType.entries.forEach { type ->
                val label = when (type) {
                    FanCardType.RATING -> stringResource(R.string.fancard_type_rating)
                    FanCardType.TOP_MONTH -> stringResource(R.string.fancard_type_top)
                    FanCardType.PROFILE -> stringResource(R.string.fancard_type_profile)
                    FanCardType.STATS -> stringResource(R.string.fancard_type_stats)
                }
                val selected = type == selectedType
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) fanColors.gradientAccent.first()
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedType = type },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (selectedType) {
                FanCardType.RATING -> RatingFanCard(
                    graphicsLayer = graphicsLayer,
                    userName = state.userName,
                    item = state.topCompleted.firstOrNull(),
                    fanColors = fanColors,
                )
                FanCardType.TOP_MONTH -> TopMonthFanCard(
                    graphicsLayer = graphicsLayer,
                    userName = state.userName,
                    items = state.topCompleted,
                    avgRating = state.avgRating,
                    fanColors = fanColors,
                )
                FanCardType.PROFILE -> ProfileFanCard(
                    graphicsLayer = graphicsLayer,
                    userName = state.userName,
                    stats = state.stats,
                    fanColors = fanColors,
                )
                FanCardType.STATS -> StatsFanCard(
                    graphicsLayer = graphicsLayer,
                    userName = state.userName,
                    stats = state.stats,
                    detailed = state.detailedStats,
                    fanColors = fanColors,
                )
            }

            Spacer(Modifier.height(16.dp))

            FilledTonalButton(
                onClick = {
                    scope.launch {
                        FanCardShareHelper.shareBitmap(context, graphicsLayer)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = fanColors.gradientAccent.first(),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.fancard_share),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun Modifier.captureLayer(graphicsLayer: GraphicsLayer): Modifier =
    this.drawWithContent {
        graphicsLayer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(graphicsLayer)
    }

@Composable
private fun StatsFanCard(
    graphicsLayer: GraphicsLayer,
    userName: String,
    stats: com.mediatracker.domain.model.UserStats,
    detailed: com.mediatracker.domain.model.DetailedStats,
    fanColors: FanAppColors,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .captureLayer(graphicsLayer)
            .background(Brush.linearGradient(fanColors.gradient2), RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "📊", fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.fancard_stats_title),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatPill("✅", detailed.totalCompleted)
                StatPill("⏱️", detailed.estimatedHours)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("🎬", detailed.seriesCompleted)
                StatPill("🎥", detailed.moviesCompleted)
                StatPill("📖", detailed.booksCompleted)
            }
            if (detailed.topGenres.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = detailed.topGenres.take(3).joinToString(" · ") { it.genre },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${stats.levelIcon} Nv.${stats.level} · ${stats.levelTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "FanApp",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProfileFanCard(
    graphicsLayer: GraphicsLayer,
    userName: String,
    stats: com.mediatracker.domain.model.UserStats,
    fanColors: FanAppColors,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .captureLayer(graphicsLayer)
            .background(Brush.linearGradient(fanColors.gradient2), RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "✨", fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.22f),
            ) {
                Text(
                    text = "${stats.levelIcon} Nv.${stats.level} · ${stats.levelTitle}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatPill("🎬", stats.seriesCompleted)
                StatPill("🎥", stats.moviesCompleted)
                StatPill("📖", stats.booksCompleted)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "FanApp",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatPill(icon: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(icon, fontSize = 18.sp)
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun TopMonthFanCard(
    graphicsLayer: GraphicsLayer,
    userName: String,
    items: List<UserItem>,
    avgRating: Float,
    fanColors: FanAppColors,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .captureLayer(graphicsLayer)
            .background(Brush.linearGradient(fanColors.gradient2), RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "🏆", fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.fancard_top_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
            if (avgRating > 0f) {
                Text(
                    text = "★ ${"%.1f".format(avgRating)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(12.dp))
            items.take(5).forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.userRating != null) {
                        MiniStars(item.userRating, fanColors)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "— $userName · FanApp",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun RatingFanCard(
    graphicsLayer: GraphicsLayer,
    userName: String,
    item: UserItem?,
    fanColors: FanAppColors,
) {
    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .captureLayer(graphicsLayer)
                .background(Brush.linearGradient(fanColors.gradient2), RoundedCornerShape(28.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.fancard_no_items),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
        }
        return
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .captureLayer(graphicsLayer)
            .background(Brush.linearGradient(fanColors.gradient2), RoundedCornerShape(28.dp))
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!item.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientPoster(
                    title = item.title,
                    kind = item.mediaType,
                    modifier = Modifier
                        .size(width = 120.dp, height = 180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            item.userRating?.let { rating ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star
                            else Icons.Filled.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (star <= rating) Color(0xFFFFC107)
                            else Color.White.copy(alpha = 0.3f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— $userName · FanApp",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun MiniStars(rating: Int, fanColors: FanAppColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (star <= rating) Color(0xFFFFC107)
                else Color.White.copy(alpha = 0.3f),
            )
        }
    }
}
