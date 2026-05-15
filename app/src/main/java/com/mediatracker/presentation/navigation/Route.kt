package com.mediatracker.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object MainGraph : Route
    @Serializable data class Detail(val mediaId: String) : Route
}

sealed interface BottomNavRoute {
    @Serializable data object Home : BottomNavRoute
    @Serializable data object Discover : BottomNavRoute
    @Serializable data object Library : BottomNavRoute
    @Serializable data object Profile : BottomNavRoute
}
