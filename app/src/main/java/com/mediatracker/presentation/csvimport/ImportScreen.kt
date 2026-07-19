package com.mediatracker.presentation.csvimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassBackHeader
import com.mediatracker.domain.usecase.ImportSource
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors

@Composable
fun ImportScreen(
    onBack: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fanColors = MaterialTheme.fanAppColors
    val context = LocalContext.current

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        viewModel.parseFile(uri, context.contentResolver)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(Modifier.statusBarsPadding())
        GlassBackHeader(
            title = stringResource(R.string.import_title),
            onBack = onBack,
        )

        when (state.step) {
            ImportStep.SOURCE_SELECTION -> SourceSelectionStep(
                onSelect = { source ->
                    viewModel.selectSource(source)
                    val mimeTypes = arrayOf("text/csv", "text/comma-separated-values", "application/csv", "*/*")
                    fileLauncher.launch(mimeTypes)
                },
            )

            ImportStep.PREVIEW -> PreviewStep(
                preview = state.preview,
                source = state.source,
                onConfirm = { viewModel.confirmImport() },
                onCancel = { viewModel.reset() },
            )

            ImportStep.IMPORTING -> ImportingStep(
                progress = state.progress,
                total = state.total,
            )

            ImportStep.DONE -> DoneStep(
                result = state.result,
                onDone = onBack,
                onImportMore = { viewModel.reset() },
            )
        }

        state.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceSelectionStep(onSelect: (ImportSource) -> Unit) {
    val fanColors = MaterialTheme.fanAppColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Importa tu biblioteca",
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            "Importa tu lista desde Letterboxd o Goodreads y no pierdes tu historial.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        ImportSourceCard(
            icon = "🎬",
            title = "Letterboxd",
            subtitle = "Exporta CSV desde Settings → Import & Export",
            onClick = { onSelect(ImportSource.LETTERBOXD) },
        )

        ImportSourceCard(
            icon = "📖",
            title = "Goodreads",
            subtitle = "Exporta CSV desde My Books → Import/Export",
            onClick = { onSelect(ImportSource.GOODREADS) },
        )
    }
}

@Composable
private fun ImportSourceCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(icon, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PreviewStep(
    preview: com.mediatracker.data.csvimport.ImportPreview?,
    source: ImportSource?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    if (preview == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        val sourceName = when (source) {
            ImportSource.LETTERBOXD -> "Letterboxd"
            ImportSource.GOODREADS -> "Goodreads"
            else -> ""
        }

        Text(
            "Vista previa — $sourceName",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = DisplayFontFamily),
        )

        Spacer(Modifier.height(16.dp))

        PreviewStatRow("Total items", preview.totalItems.toString())
        PreviewStatRow("Completados", "${preview.completed} ✅")
        PreviewStatRow("En lista", "${preview.watchlist} 🔖")
        if (preview.inProgress > 0) PreviewStatRow("En progreso", "${preview.inProgress} 🔄")
        if (preview.abandoned > 0) PreviewStatRow("Abandonados", "${preview.abandoned} 🚫")

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Importar ${preview.totalItems} items")
            }
        }
    }
}

@Composable
private fun PreviewStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ImportingStep(progress: Int, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            "Importando...",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = DisplayFontFamily),
        )
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) progress.toFloat() / total else 0f },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$progress / $total items",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DoneStep(
    result: com.mediatracker.data.csvimport.ImportResult?,
    onDone: () -> Unit,
    onImportMore: () -> Unit,
) {
    if (result == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            "✅ Importacion completada",
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = DisplayFontFamily),
        )

        Spacer(Modifier.height(24.dp))

        PreviewStatRow("Intentados", result.totalAttempted.toString())
        PreviewStatRow("Importados", "${result.imported} ✅")
        PreviewStatRow("Duplicados (ya existian)", "${result.duplicates} 🔖")
        if (result.failed > 0) PreviewStatRow("Fallidos", "${result.failed} ❌")

        if (result.errors.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Detalles:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
            ) {
                items(result.errors) { error ->
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onImportMore,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Importar mas")
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Hecho")
            }
        }
    }
}
