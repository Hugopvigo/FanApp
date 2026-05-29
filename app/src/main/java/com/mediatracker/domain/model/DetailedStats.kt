package com.mediatracker.domain.model

data class GenreCount(
    val genre: String,
    val count: Int,
)

data class DetailedStats(
    val totalCompleted: Int = 0,
    val totalInProgress: Int = 0,
    val totalAbandoned: Int = 0,
    val totalWatchlist: Int = 0,
    val seriesCompleted: Int = 0,
    val moviesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val estimatedHours: Int = 0,
    val topGenres: List<GenreCount> = emptyList(),
    val monthlyActivity: List<MonthlyActivityPoint> = emptyList(),
)

data class MonthlyActivityPoint(
    val monthLabel: String,
    val count: Int,
)
