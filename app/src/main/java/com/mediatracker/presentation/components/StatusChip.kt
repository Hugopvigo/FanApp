package com.mediatracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.mediatracker.R
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.presentation.theme.FanAppColors
import com.mediatracker.presentation.theme.fanAppColors

@Composable
fun StatusChip(
    status: ItemStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fanColors = MaterialTheme.fanAppColors
    val statusColor = status.fanColor(fanColors)
    val shape = RoundedCornerShape(12.dp)

    val bgMod = if (selected) {
        Modifier.background(statusColor.copy(alpha = 0.20f))
    } else {
        Modifier.background(Color.Transparent)
    }
    val borderMod = if (selected) {
        Modifier.border(1.dp, statusColor.copy(alpha = 0.55f), shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(bgMod)
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(text = status.statusIcon, fontSize = 11.sp)
            Text(
                text = status.label(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier,
) {
    val fanColors = MaterialTheme.fanAppColors
    val statusColor = status.fanColor(fanColors)
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(statusColor.copy(alpha = 0.20f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = status.statusIcon, fontSize = 9.sp)
            Text(
                text = status.label(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
            )
        }
    }
}

@Composable
fun ItemStatus.label(): String = when (this) {
    ItemStatus.WATCHLIST    -> stringResource(R.string.status_watchlist)
    ItemStatus.IN_PROGRESS  -> stringResource(R.string.status_in_progress)
    ItemStatus.COMPLETED    -> stringResource(R.string.status_completed)
    ItemStatus.ABANDONED    -> stringResource(R.string.status_abandoned)
}

val ItemStatus.statusIcon: String
    get() = when (this) {
        ItemStatus.WATCHLIST    -> "📋"
        ItemStatus.IN_PROGRESS  -> "🔄"
        ItemStatus.COMPLETED    -> "✅"
        ItemStatus.ABANDONED    -> "❌"
    }

val ItemStatus.tabIcon: ImageVector
    get() = when (this) {
        ItemStatus.WATCHLIST    -> Icons.Outlined.Bookmark
        ItemStatus.IN_PROGRESS  -> Icons.Outlined.PlayCircle
        ItemStatus.COMPLETED    -> Icons.Outlined.CheckCircle
        ItemStatus.ABANDONED    -> Icons.Outlined.Cancel
    }

fun ItemStatus.fanColor(fanColors: FanAppColors): Color = when (this) {
    ItemStatus.WATCHLIST    -> fanColors.statusWant
    ItemStatus.IN_PROGRESS  -> fanColors.statusProgress
    ItemStatus.COMPLETED    -> fanColors.statusDone
    ItemStatus.ABANDONED    -> fanColors.statusDropped
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    MediaTrackerTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusChip(status = ItemStatus.WATCHLIST, selected = true, onClick = {})
            StatusChip(status = ItemStatus.IN_PROGRESS, selected = false, onClick = {})
            StatusChip(status = ItemStatus.COMPLETED, selected = false, onClick = {})
            StatusChip(status = ItemStatus.ABANDONED, selected = false, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    MediaTrackerTheme {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(status = ItemStatus.WATCHLIST)
            StatusBadge(status = ItemStatus.IN_PROGRESS)
            StatusBadge(status = ItemStatus.COMPLETED)
            StatusBadge(status = ItemStatus.ABANDONED)
        }
    }
}
