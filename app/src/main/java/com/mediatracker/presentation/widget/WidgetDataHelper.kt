package com.mediatracker.presentation.widget

import android.content.Context
import com.mediatracker.data.local.UserItemDao
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import timber.log.Timber

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun userItemDao(): UserItemDao
}

object WidgetDataHelper {

    suspend fun getInProgressItems(context: Context): List<WidgetItem> {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                WidgetEntryPoint::class.java
            )
            val dao = entryPoint.userItemDao()
            val entities = dao.getByStatus(ItemStatus.IN_PROGRESS.name).first()
            entities.sortedByDescending { it.updatedAt }.take(1).map { it.toWidgetItem() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load widget data")
            emptyList()
        }
    }

    private fun com.mediatracker.data.local.UserItemEntity.toWidgetItem(): WidgetItem {
        val mediaTypeEnum = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.MOVIE)
        val label = buildProgressLabel()
        return WidgetItem(
            id = id,
            apiId = apiId,
            mediaType = mediaTypeEnum,
            title = title.ifBlank { apiId },
            posterUrl = posterUrl,
            status = runCatching { ItemStatus.valueOf(status) }.getOrDefault(ItemStatus.IN_PROGRESS),
            progressLabel = label,
        )
    }

    private fun com.mediatracker.data.local.UserItemEntity.buildProgressLabel(): String? {
        return when {
            mediaType == MediaType.SERIES.name && currentSeason != null && currentEpisode != null ->
                "S${currentSeason}E${currentEpisode}"
            mediaType == MediaType.BOOK.name && currentPage != null && totalPages != null && totalPages > 0 ->
                "$currentPage/$totalPages"
            mediaType == MediaType.BOOK.name && currentPage != null ->
                "P. $currentPage"
            else -> null
        }
    }
}
