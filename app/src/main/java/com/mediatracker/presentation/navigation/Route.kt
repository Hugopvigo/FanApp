package com.mediatracker.presentation.navigation

import com.mediatracker.domain.model.MediaType
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object MainGraph : Route
    @Serializable data class Detail(val apiId: String, val mediaType: MediaType) : Route
    @Serializable data object Theme : Route
    @Serializable data object ChangePassword : Route
    @Serializable data object Notifications : Route
    @Serializable data object Privacy : Route
    @Serializable data class FanCard(
        val preset: String? = null,
        val ratingApiId: String? = null,
        val ratingMediaType: String? = null,
        val ratingValue: Int? = null,
    ) : Route
    @Serializable data object Achievements : Route
    @Serializable data object Leaderboard : Route
    @Serializable data object Import : Route
    @Serializable data object Stats : Route
    @Serializable data object Onboarding : Route
}

sealed interface BottomNavRoute {
    @Serializable data object Home : BottomNavRoute
    @Serializable data object Discover : BottomNavRoute
    @Serializable data object Library : BottomNavRoute
    @Serializable data object Profile : BottomNavRoute
}
