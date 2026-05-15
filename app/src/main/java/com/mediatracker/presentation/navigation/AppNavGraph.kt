package com.mediatracker.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mediatracker.presentation.auth.AuthViewModel
import com.mediatracker.presentation.auth.LoginScreen
import com.mediatracker.presentation.discover.DiscoverScreen
import com.mediatracker.presentation.home.HomeScreen
import com.mediatracker.presentation.library.LibraryScreen
import com.mediatracker.presentation.profile.ProfileScreen

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: BottomNavRoute,
)

private val bottomNavItems = listOf(
    BottomNavItem("Inicio", Icons.Default.Home, BottomNavRoute.Home),
    BottomNavItem("Buscar", Icons.Default.Search, BottomNavRoute.Discover),
    BottomNavItem("Biblioteca", Icons.AutoMirrored.Filled.LibraryBooks, BottomNavRoute.Library),
    BottomNavItem("Perfil", Icons.Default.Person, BottomNavRoute.Profile),
)

@Composable
fun AppNavGraph() {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.state.collectAsStateWithLifecycle()

    NavHost(
        navController = rootNavController,
        startDestination = if (uiState.isLoggedIn) Route.MainGraph else Route.Login,
    ) {
        composable<Route.Login> {
            LoginScreen(
                isLoading = uiState.isLoading,
                error = uiState.error,
                isLoggedIn = uiState.isLoggedIn,
                onLogin = { email, password -> authViewModel.login(email, password) },
                onRegister = { name, email, password -> authViewModel.register(name, email, password) },
                onErrorDismiss = { authViewModel.clearError() },
                onLoginSuccess = {
                    rootNavController.navigate(Route.MainGraph) {
                        popUpTo<Route.Login> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.MainGraph> {
            MainScreen(authViewModel = authViewModel)
        }
    }
}

@Composable
private fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true
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
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavRoute.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable<BottomNavRoute.Home> { HomeScreen() }
            composable<BottomNavRoute.Discover> { DiscoverScreen() }
            composable<BottomNavRoute.Library> { LibraryScreen() }
            composable<BottomNavRoute.Profile> {
                ProfileScreen(
                    userEmail = authViewModel.state.value.userEmail,
                    userName = authViewModel.state.value.userName,
                    onLogout = { authViewModel.logout() },
                )
            }
        }
    }
}
