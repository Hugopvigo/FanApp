package com.mediatracker.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.components.ErrorState
import com.mediatracker.presentation.components.FavoriteToggle
import com.mediatracker.presentation.components.LoadingState
import com.mediatracker.presentation.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.title ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.error != null -> ErrorState(state.error ?: "Error desconocido", modifier = Modifier.padding(padding))
            state.item != null -> DetailContent(
                state = state,
                onStatusSelected = viewModel::onStatusSelected,
                onRemoveFromList = viewModel::onRemoveFromList,
                onToggleFavorite = viewModel::onToggleFavorite,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    onStatusSelected: (ItemStatus) -> Unit,
    onRemoveFromList: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.item ?: return
    val userItem = state.userItem

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
        )

        if (item.releaseDate.isNotBlank()) {
            Text(
                text = item.releaseDate.take(4),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (item.genres.isNotEmpty()) {
            Text(
                text = item.genres.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (item.rating > 0f) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%.1f / 10".format(item.rating),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.overview,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
        )

        item.extraData?.let { extra ->
            Spacer(modifier = Modifier.height(16.dp))
            ExtraInfo(extra = extra, mediaType = item.mediaType)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                    Text("Quitar")
                }
            }
        }

        if (userItem != null) {
            Spacer(modifier = Modifier.height(8.dp))
            FavoriteToggle(
                isFavorite = userItem.favorite,
                onToggle = onToggleFavorite,
                enabled = userItem.status != ItemStatus.ABANDONED,
            )
        }
    }
}

@Composable
private fun ExtraInfo(extra: Map<String, String>, mediaType: MediaType) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        extra.forEach { (key, value) ->
            val label = when (key) {
                "numberOfSeasons" -> "Temporadas"
                "numberOfEpisodes" -> "Episodios"
                "creators" -> "Creadores"
                "runtime" -> "Duración"
                "authors" -> "Autor"
                "publisher" -> "Editorial"
                "pageCount" -> "Páginas"
                else -> key
            }
            val displayValue = when (key) {
                "runtime" -> "$value min"
                else -> value
            }
            Row {
                Text(
                    text = "$label: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
