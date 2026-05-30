package com.mediatracker.domain.model

data class Ranking(
    val id: String,
    val userId: String,
    val displayName: String,
    val avatarId: String?,
    val xp: Int,
    val level: Int,
    val totalCompleted: Int,
    val seriesCompleted: Int = 0,
    val moviesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val rank: Int,
)
