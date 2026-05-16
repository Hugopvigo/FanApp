package com.mediatracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediatracker.domain.model.ItemStatus

@Composable
fun StatusChip(
    status: ItemStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(status.label) },
        modifier = modifier.padding(end = 4.dp),
    )
}

val ItemStatus.label: String
    get() = when (this) {
        ItemStatus.WATCHLIST -> "Quiero ver"
        ItemStatus.IN_PROGRESS -> "En progreso"
        ItemStatus.COMPLETED -> "Completado"
        ItemStatus.ABANDONED -> "Abandonado"
    }
