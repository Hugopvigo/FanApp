package com.mediatracker.presentation.detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mediatracker.R
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors

@Composable
internal fun HeroBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.40f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
internal fun ExtraInfo(extra: Map<String, String>, mediaType: MediaType) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        extra.forEach { (key, value) ->
            val label = when (key) {
                "numberOfSeasons" -> stringResource(R.string.detail_seasons)
                "numberOfEpisodes" -> stringResource(R.string.detail_episodes)
                "creators" -> stringResource(R.string.detail_creators)
                "runtime" -> stringResource(R.string.detail_runtime)
                "authors" -> stringResource(R.string.detail_author)
                "publisher" -> stringResource(R.string.detail_publisher)
                "pageCount" -> stringResource(R.string.detail_pages)
                else -> key
            }
            val displayValue = if (key == "runtime") "$value min" else value
            Row {
                Text(
                    text = "$label: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = displayValue, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
internal fun StarRatingBar(
    rating: Int?,
    onRatingChanged: (Int?) -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    Column {
        Text(
            text = stringResource(R.string.detail_your_rating),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { star ->
                val selected = rating != null && star <= rating
                Icon(
                    imageVector = if (selected) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "$star",
                    tint = if (selected) fanColors.gradientAccent.first()
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            onRatingChanged(if (rating == star) null else star)
                        },
                )
            }
        }
    }
}

@Composable
internal fun NotesField(
    notes: String,
    onNotesChanged: (String) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.detail_notes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            placeholder = { Text(stringResource(R.string.detail_notes_hint)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
internal fun SeasonEpisodeStepper(
    season: Int?,
    episode: Int?,
    onChanged: (Int?, Int?) -> Unit,
) {
    val s = season ?: 1
    val e = episode ?: 1

    Column {
        Text(
            text = stringResource(R.string.detail_tracking),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepperControl(
                label = stringResource(R.string.detail_season),
                value = s,
                onDecrement = { onChanged(maxOf(1, s - 1), e) },
                onIncrement = { onChanged(s + 1, 1) },
                modifier = Modifier.weight(1f),
            )
            StepperControl(
                label = stringResource(R.string.detail_episode),
                value = e,
                onDecrement = { onChanged(s, maxOf(1, e - 1)) },
                onIncrement = { onChanged(s, e + 1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun GoToPageDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pageInput by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_go_to_page_title)) },
        text = {
            OutlinedTextField(
                value = pageInput,
                onValueChange = { pageInput = it.filter { ch -> ch.isDigit() } },
                label = { Text(stringResource(R.string.detail_go_to_page_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pageInput) },
                enabled = pageInput.trim().toIntOrNull() != null && (pageInput.trim().toIntOrNull() ?: 0) > 0,
            ) {
                Text(stringResource(R.string.detail_go_to_page_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_cancel))
            }
        },
    )
}

@Composable
internal fun PageProgressStepper(
    currentPage: Int?,
    totalPages: Int?,
    apiPageCount: Int?,
    onChanged: (Int?, Int?) -> Unit,
    onGoToPageClick: () -> Unit,
) {
    val t = totalPages ?: apiPageCount ?: 0
    val c = currentPage ?: 0
    val fanColors = MaterialTheme.fanAppColors

    Column {
        Text(
            text = stringResource(R.string.detail_page_tracking),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepperControl(
                label = stringResource(R.string.detail_current_page),
                value = c,
                onDecrement = { onChanged(maxOf(0, c - 1), t) },
                onIncrement = { onChanged(minOf(if (t > 0) t else c + 1, c + 1), t) },
                modifier = Modifier.weight(1f),
                allowZero = true,
            )
            StepperControl(
                label = stringResource(R.string.detail_total_pages),
                value = t,
                onDecrement = { onChanged(minOf(c, maxOf(1, t - 1)), maxOf(1, t - 1)) },
                onIncrement = { onChanged(c, t + 1) },
                modifier = Modifier.weight(1f),
                allowZero = true,
            )
        }
        if (t > 0 && c > 0) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { c.toFloat() / t.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = fanColors.gradientAccent.first(),
                trackColor = fanColors.surfaceAlpha.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(c.toFloat() / t.toFloat() * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.detail_go_to_page_hint),
            style = MaterialTheme.typography.labelMedium,
            color = fanColors.gradientAccent.first(),
            modifier = Modifier.clickable(onClick = onGoToPageClick),
        )
    }
}

@Composable
internal fun StepperControl(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    allowZero: Boolean = false,
) {
    val fanColors = MaterialTheme.fanAppColors
    val minValue = if (allowZero) 0 else 1

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = fanColors.surfaceAlpha.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, fanColors.borderColor),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onDecrement, enabled = value > minValue) {
                    Icon(Icons.Default.Remove, contentDescription = "-", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = fanColors.gradientAccent.first(),
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "+", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
