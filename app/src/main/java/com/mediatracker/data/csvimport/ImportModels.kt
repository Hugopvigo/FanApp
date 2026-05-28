package com.mediatracker.data.csvimport

import com.mediatracker.domain.model.ItemStatus
import kotlinx.serialization.Serializable

@Serializable
data class ImportItem(
    val title: String,
    val year: String,
    val mediaType: String,
    val status: ItemStatus,
    val originalLine: Int,
)

data class ImportPreview(
    val totalItems: Int,
    val completed: Int,
    val watchlist: Int,
    val inProgress: Int,
    val abandoned: Int,
    val items: List<ImportItem>,
)

data class ImportResult(
    val totalAttempted: Int,
    val imported: Int,
    val duplicates: Int,
    val failed: Int,
    val errors: List<String>,
)
