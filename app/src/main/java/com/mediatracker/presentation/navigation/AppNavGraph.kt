package com.mediatracker.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mediatracker.R
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.auth.AuthViewModel
import com.mediatracker.presentation.auth.LoginScreen
import com.mediatracker.presentation.detail.DetailScreen
import com.mediatracker.presentation.discover.DiscoverScreen
import com.mediatracker.presentation.home.HomeScreen
import com.mediatracker.presentation.library.LibraryScreen
import com.mediatracker.presentation.profile.ChangePasswordScreen
import com.mediatracker.presentation.profile.NotificationsScreen
import com.mediatracker.presentation.profile.PrivacyScreen
import com.mediatracker.presentation.profile.ProfileScreen
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.LocalAppTheme
import com.mediatracker.presentation.theme.ThemeScreen
import com.mediatracker.presentation.theme.fanAppColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

private data class BottomNavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: BottomNavRoute,
)

private val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, Icons.Default.Home, BottomNavRoute.Home),
    BottomNavItem(R.string.nav_discover, Icons.Default.Search, BottomNavRoute.Discover),
    BottomNavItem(R.string.nav_library, Icons.AutoMirrored.Filled.LibraryBooks, BottomNavRoute.Library),
    BottomNavItem(R.string.nav_profile, Icons.Default.Person, BottomNavRoute.Profile),
)

@Composable
fun AppNavGraph(
    onThemeChange: (AppTheme) -> Unit = {},
) {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.state.collectAsStateWithLifecycle()

    NavHost(
        navController = rootNavController,
        startDestination = if (uiState.isLoggedIn) Route.MainGraph else Route.Login,
    ) {
        composable<Route.Login> {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate(Route.MainGraph) {
                        popUpTo<Route.Login> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.MainGraph> {
            MainScreen(
                authViewModel = authViewModel,
                onThemeChange = onThemeChange,
            )
        }
    }
}

@Composable
private fun MainScreen(
    authViewModel: AuthViewModel,
    onThemeChange: (AppTheme) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val fanColors = MaterialTheme.fanAppColors

    val showBottomBar = currentDestination?.hasRoute(Route.Detail::class) != true &&
        currentDestination?.hasRoute(Route.Theme::class) != true &&
        currentDestination?.hasRoute(Route.Notifications::class) != true &&
        currentDestination?.hasRoute(Route.Privacy::class) != true &&
        currentDestination?.hasRoute(Route.ChangePassword::class) != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = fanColors.navBarBg) {
                    bottomNavItems.forEach { item ->
                        val selected = when (item.route) {
                            BottomNavRoute.Home     -> currentDestination?.hasRoute(BottomNavRoute.Home::class) == true
                            BottomNavRoute.Discover -> currentDestination?.hasRoute(BottomNavRoute.Discover::class) == true
                            BottomNavRoute.Library  -> currentDestination?.hasRoute(BottomNavRoute.Library::class) == true
                            BottomNavRoute.Profile  -> currentDestination?.hasRoute(BottomNavRoute.Profile::class) == true
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes), style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home,
            modifier = Modifier.padding(padding),
            // Crossfade suave para cambios de tab
            enterTransition = { fadeIn(tween(180, easing = FastOutSlowInEasing)) },
            exitTransition = { fadeOut(tween(120, easing = FastOutSlowInEasing)) },
            popEnterTransition = { fadeIn(tween(180, easing = FastOutSlowInEasing)) },
            popExitTransition = { fadeOut(tween(120, easing = FastOutSlowInEasing)) },
        ) {
            composable<BottomNavRoute.Home> {
                HomeScreen(
                    onItemClick = { item ->
                        navController.navigate(Route.Detail(item.apiId, item.mediaType))
                    },
                )
            }
            composable<BottomNavRoute.Discover> {
                DiscoverScreen(
                    onItemClick = { item ->
                        navController.navigate(Route.Detail(item.apiId, item.mediaType))
                    },
                )
            }
            composable<BottomNavRoute.Library> {
                LibraryScreen(
                    onItemClick = { userItem ->
                        navController.navigate(Route.Detail(userItem.apiId, userItem.mediaType))
                    },
                )
            }
            composable<BottomNavRoute.Profile> {
                ProfileScreen(
                    onLogout = { authViewModel.logout() },
                    onNavigateToTheme = { navController.navigate(Route.Theme) },
                    onNavigateToChangePassword = { navController.navigate(Route.ChangePassword) },
                    onNavigateToNotifications = { navController.navigate(Route.Notifications) },
                    onNavigateToPrivacy = { navController.navigate(Route.Privacy) },
                )
            }

            // Detail: slide desde la derecha + fade
            composable<Route.Detail>(
                enterTransition = {
                    fadeIn(tween(280)) +
                        slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 3 }
                },
                exitTransition = {
                    fadeOut(tween(180)) +
                        slideOutHorizontally(tween(180)) { -it / 6 }
                },
                popEnterTransition = {
                    fadeIn(tween(280)) +
                        slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it / 3 }
                },
                popExitTransition = {
                    fadeOut(tween(220)) +
                        slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 3 }
                },
            ) {
                DetailScreen(onBack = { navController.popBackStack() })
            }

            // Sub-pantallas de Profile: slide hacia arriba (efecto sheet)
            composable<Route.Theme>(
                enterTransition = {
                    fadeIn(tween(260)) +
                        slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 }
                },
                popExitTransition = {
                    fadeOut(tween(200)) +
                        slideOutVertically(tween(200)) { it / 2 }
                },
            ) {
                ThemeScreen(
                    currentTheme = LocalAppTheme.current,
                    onThemeSelected = { theme ->
                        onThemeChange(theme)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.ChangePassword>(
                enterTransition = {
                    fadeIn(tween(260)) +
                        slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 }
                },
                popExitTransition = {
                    fadeOut(tween(200)) +
                        slideOutVertically(tween(200)) { it / 2 }
                },
            ) {
                ChangePasswordScreen(onBack = { navController.popBackStack() })
            }
            composable<Route.Notifications>(
                enterTransition = {
                    fadeIn(tween(260)) +
                        slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 }
                },
                popExitTransition = {
                    fadeOut(tween(200)) +
                        slideOutVertically(tween(200)) { it / 2 }
                },
            ) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable<Route.Privacy>(
                enterTransition = {
                    fadeIn(tween(260)) +
                        slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it / 2 }
                },
                popExitTransition = {
                    fadeOut(tween(200)) +
                        slideOutVertically(tween(200)) { it / 2 }
                },
            ) {
                PrivacyScreen(
                    onBack = { navController.popBackStack() },
                    onDeleteAccount = { authViewModel.logout() },
                )
            }
        }
    }
}

private val MediaItem.apiId: String
    get() = id.removePrefix("${mediaType.name.lowercase()}_")
