package com.mediatracker.domain.model

data class UserStats(
    val seriesInProgress: Int = 0,
    val seriesCompleted: Int = 0,
    val moviesInProgress: Int = 0,
    val moviesCompleted: Int = 0,
    val booksInProgress: Int = 0,
    val booksCompleted: Int = 0,
    val totalXp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Novato",
    val levelIcon: String = "\uD83C\uDF31",
)
