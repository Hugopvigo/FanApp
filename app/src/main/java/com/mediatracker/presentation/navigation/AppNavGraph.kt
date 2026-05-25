package com.mediatracker.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search

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

    val showBottomBar = currentDestination?.hasRoute(Route.Detail::class) != true &&
        currentDestination?.hasRoute(Route.Theme::class) != true &&
        currentDestination?.hasRoute(Route.Notifications::class) != true &&
        currentDestination?.hasRoute(Route.Privacy::class) != true &&
        currentDestination?.hasRoute(Route.ChangePassword::class) != true

    val navItems = makeNavItems(
        labelResHome     = R.string.nav_home,
        labelResDiscover = R.string.nav_discover,
        labelResLibrary  = R.string.nav_library,
        labelResProfile  = R.string.nav_profile,
        iconHome         = Icons.Default.Home,
        iconDiscover     = Icons.Default.Search,
        iconLibrary      = Icons.AutoMirrored.Filled.LibraryBooks,
        iconProfile      = Icons.Default.Person,
    )

    Scaffold(
        // Transparent spacer so the NavHost content is pushed up above the floating nav
        bottomBar = {
            if (showBottomBar) {
                Spacer(Modifier.navigationBarsPadding().padding(bottom = 80.dp))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavRoute.Home,
                modifier = Modifier.fillMaxSize(),
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
                composable<Route.Detail> {
                    DetailScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<Route.Theme> {
                    ThemeScreen(
                        currentTheme = LocalAppTheme.current,
                        onThemeSelected = { theme ->
                            onThemeChange(theme)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<Route.ChangePassword> {
                    ChangePasswordScreen(onBack = { navController.popBackStack() })
                }
                composable<Route.Notifications> {
                    NotificationsScreen(onBack = { navController.popBackStack() })
                }
                composable<Route.Privacy> {
                    PrivacyScreen(
                        onBack = { navController.popBackStack() },
                        onDeleteAccount = { authViewModel.logout() },
                    )
                }
            }

            // Floating glass nav bar overlaid at the bottom
            if (showBottomBar) {
                FloatingBottomNav(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    items = navItems,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                )
            }
        }
    }
}

private val MediaItem.apiId: String
    get() = id.removePrefix("${mediaType.name.lowercase()}_")
