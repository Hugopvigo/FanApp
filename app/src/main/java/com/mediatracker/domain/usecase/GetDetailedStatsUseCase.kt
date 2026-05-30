package com.mediatracker.domain.usecase

import com.mediatracker.data.local.MediaItemDao
import com.mediatracker.data.local.toDomain
import com.mediatracker.domain.model.DetailedStats
import com.mediatracker.domain.model.GenreCount
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.MonthlyActivityPoint
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetDetailedStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val mediaItemDao: MediaItemDao,
) {
    operator fun invoke(): Flow<DetailedStats> =
        userRepository.getUserItemsFlow()
            .flatMapLatest { items ->
                flow { emit(buildStats(items)) }
            }
            .flowOn(Dispatchers.IO)

    private suspend fun buildStats(items: List<UserItem>): DetailedStats {
        val completed = items.filter { it.status == ItemStatus.COMPLETED }
        val inProgress = items.filter { it.status == ItemStatus.IN_PROGRESS }
        val abandoned = items.filter { it.status == ItemStatus.ABANDONED }
        val watchlist = items.filter { it.status == ItemStatus.WATCHLIST }

        return DetailedStats(
            totalCompleted = completed.size,
            totalInProgress = inProgress.size,
            totalAbandoned = abandoned.size,
            totalWatchlist = watchlist.size,
            seriesCompleted = completed.count { it.mediaType == MediaType.SERIES },
            moviesCompleted = completed.count { it.mediaType == MediaType.MOVIE },
            booksCompleted = completed.count { it.mediaType == MediaType.BOOK },
            estimatedHours = calculateEstimatedHours(items),
            topGenres = calculateTopGenres(completed),
            monthlyActivity = calculateMonthlyActivity(completed),
        )
    }

    private suspend fun calculateTopGenres(completed: List<UserItem>): List<GenreCount> {
        val counts = mutableMapOf<String, Int>()
        for (item in completed) {
            val mediaId = "${item.mediaType.name.lowercase()}_${item.apiId}"
            val media = mediaItemDao.getById(mediaId)?.toDomain() ?: continue
            for (genre in media.genres) {
                val key = genre.trim()
                if (key.isNotEmpty()) {
                    counts[key] = (counts[key] ?: 0) + 1
                }
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { GenreCount(it.key, it.value) }
    }

    private fun calculateMonthlyActivity(completed: List<UserItem>): List<MonthlyActivityPoint> {
        val now = Calendar.getInstance()
        val fmt = SimpleDateFormat("MMM", Locale.getDefault())
        val result = mutableListOf<MonthlyActivityPoint>()

        for (i in 11 downTo 0) {
            val cal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -i) }
            val monthLabel = fmt.format(cal.time)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            val count = completed.count { item ->
                val itemCal = Calendar.getInstance().apply { timeInMillis = item.updatedAt }
                itemCal.get(Calendar.MONTH) == month && itemCal.get(Calendar.YEAR) == year
            }
            result.add(MonthlyActivityPoint(monthLabel, count))
        }
        return result
    }

    private suspend fun calculateEstimatedHours(items: List<UserItem>): Int {
        var minutes = 0
        for (item in items) {
            if (item.status == ItemStatus.ABANDONED) continue
            val mediaId = "${item.mediaType.name.lowercase()}_${item.apiId}"
            val media = mediaItemDao.getById(mediaId)?.toDomain()
            when (item.mediaType) {
                MediaType.SERIES -> {
                    val watchedCount = item.watchedEpisodes.values.sumOf { eps -> eps.size }
                    minutes += if (watchedCount > 0) {
                        watchedCount * 45
                    } else {
                        val season = item.currentSeason ?: 1
                        val episode = item.currentEpisode ?: 1
                        ((season - 1).coerceAtLeast(0) * 10 + episode) * 45
                    }
                }
                MediaType.MOVIE -> {
                    val runtime = media?.extraData?.get("runtime")?.toIntOrNull() ?: 120
                    minutes += runtime
                }
                MediaType.BOOK -> {
                    val pages = item.totalPages
                        ?: media?.extraData?.get("pageCount")?.toIntOrNull()
                        ?: 300
                    val readPages = when (item.status) {
                        ItemStatus.COMPLETED -> pages
                        ItemStatus.IN_PROGRESS -> item.currentPage ?: (pages / 2)
                        else -> 0
                    }
                    minutes += readPages * 2
                }
            }
        }
        return (minutes / 60).coerceAtLeast(0)
    }
}
