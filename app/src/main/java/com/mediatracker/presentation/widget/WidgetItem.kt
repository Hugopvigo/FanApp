package com.mediatracker.presentation.widget

data class WidgetItem(
    val id: String,
    val apiId: String,
    val mediaType: com.mediatracker.domain.model.MediaType,
    val title: String,
    val posterUrl: String?,
    val status: com.mediatracker.domain.model.ItemStatus,
    val progressLabel: String?,
)
