package com.mediatracker.presentation.csvimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediatracker.data.csvimport.ImportItem
import com.mediatracker.data.csvimport.ImportPreview
import com.mediatracker.data.csvimport.ImportResult
import com.mediatracker.domain.usecase.ImportSource
import com.mediatracker.domain.usecase.ImportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val step: ImportStep = ImportStep.SOURCE_SELECTION,
    val source: ImportSource? = null,
    val preview: ImportPreview? = null,
    val result: ImportResult? = null,
    val progress: Int = 0,
    val total: Int = 0,
    val isImporting: Boolean = false,
    val error: String? = null,
)

enum class ImportStep {
    SOURCE_SELECTION,
    PREVIEW,
    IMPORTING,
    DONE,
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importUseCase: ImportUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportUiState())
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    private var pendingItems: List<ImportItem> = emptyList()

    fun selectSource(source: ImportSource) {
        _state.update { it.copy(source = source) }
    }

    fun parseFile(uri: Uri?, contentResolver: android.content.ContentResolver) {
        if (uri == null) {
            _state.update { it.copy(error = "No se pudo leer el archivo") }
            return
        }
        val source = _state.value.source ?: return
        viewModelScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("No se pudo abrir el archivo")
                val preview = importUseCase.preview(inputStream, source)
                pendingItems = preview.items
                _state.update {
                    it.copy(step = ImportStep.PREVIEW, preview = preview, error = null)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al leer el archivo: ${e.message}") }
            }
        }
    }

    fun confirmImport() {
        if (pendingItems.isEmpty()) return
        _state.update { it.copy(step = ImportStep.IMPORTING, isImporting = true, progress = 0, total = pendingItems.size) }
        viewModelScope.launch {
            try {
                val result = importUseCase.import(pendingItems) { current, total ->
                    _state.update { it.copy(progress = current, total = total) }
                }
                _state.update {
                    it.copy(step = ImportStep.DONE, result = result, isImporting = false)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error en la importacion: ${e.message}", isImporting = false) }
            }
        }
    }

    fun reset() {
        pendingItems = emptyList()
        _state.update { ImportUiState() }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
