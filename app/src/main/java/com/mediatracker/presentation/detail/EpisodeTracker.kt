package com.mediatracker.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediatracker.presentation.theme.fanAppColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EpisodeTracker(
    season: Int,
    numberOfEpisodes: Int,
    watchedEpisodes: List<Int>,
    onEpisodeToggle: (Int) -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val watchedCount = watchedEpisodes.size
    val progress = if (numberOfEpisodes > 0) watchedCount.toFloat() / numberOfEpisodes.toFloat() else 0f

    Column {
        Text(
            text = "T$season Episodios",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (ep in 1..numberOfEpisodes) {
                val isWatched = ep in watchedEpisodes
                val bgColor = if (isWatched) {
                    fanColors.gradientAccent.first().copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
                val borderColor = if (isWatched) {
                    fanColors.gradientAccent.first().copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                }
                val textColor = if (isWatched) {
                    fanColors.gradientAccent.first()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable { onEpisodeToggle(ep) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isWatched) "✅" else "$ep",
                        fontSize = if (isWatched) 14.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                }
            }
        }

        if (numberOfEpisodes > 0) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = fanColors.gradientAccent.first(),
                trackColor = fanColors.surfaceAlpha.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% ($watchedCount/$numberOfEpisodes)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
