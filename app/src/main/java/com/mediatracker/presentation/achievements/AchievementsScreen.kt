package com.mediatracker.presentation.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.Achievement
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fanColors = MaterialTheme.fanAppColors

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
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${state.unlockedCount}/${state.totalCount}",
                style = MaterialTheme.typography.titleMedium,
                color = fanColors.gradientAccent.first(),
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.statusBarsPadding())
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.achievements, key = { it.id }) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val fanColors = MaterialTheme.fanAppColors
    val isUnlocked = achievement.unlockedAt != null

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            fanColors.surfaceAlpha.copy(alpha = 0.5f)
        },
        border = if (isUnlocked) {
            androidx.compose.foundation.BorderStroke(1.dp, fanColors.gradientAccent.first().copy(alpha = 0.35f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, fanColors.borderColor)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = achievement.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
            Spacer(Modifier.height(8.dp))
            if (!isUnlocked) {
                LinearProgressIndicator(
                    progress = { (achievement.progress.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = fanColors.gradientAccent.first(),
                    trackColor = fanColors.borderColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${achievement.progress}/${achievement.target}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelMedium,
                    color = fanColors.gradientAccent.first(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
