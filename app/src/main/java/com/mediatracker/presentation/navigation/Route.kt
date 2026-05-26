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
    @Serializable data object FanCard : Route
}

sealed interface BottomNavRoute {
    @Serializable data object Home : BottomNavRoute
    @Serializable data object Discover : BottomNavRoute
    @Serializable data object Library : BottomNavRoute
    @Serializable data object Profile : BottomNavRoute
}
