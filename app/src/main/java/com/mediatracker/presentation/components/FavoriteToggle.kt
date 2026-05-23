package com.mediatracker.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mediatracker.presentation.theme.MediaTrackerTheme

@Composable
fun FavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = if (isFavorite) "Quitar favorito" else "Marcar favorito",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteToggleOnPreview() {
    MediaTrackerTheme {
        FavoriteToggle(isFavorite = true, onToggle = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteToggleOffPreview() {
    MediaTrackerTheme {
        FavoriteToggle(isFavorite = false, onToggle = {})
    }
}
