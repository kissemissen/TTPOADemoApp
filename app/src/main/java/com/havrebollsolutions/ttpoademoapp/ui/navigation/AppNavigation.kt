package com.havrebollsolutions.ttpoademoapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.havrebollsolutions.ttpoademoapp.ui.screens.CartScreen
import com.havrebollsolutions.ttpoademoapp.ui.screens.MenuScreen
import com.havrebollsolutions.ttpoademoapp.ui.screens.OverviewScreen
import com.havrebollsolutions.ttpoademoapp.ui.screens.SettingsScreen
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel

sealed class Screen(
    val route: String
) {
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Overview : Screen("overview")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    menuViewModel: MenuViewModel,
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues()
) {

    Column(
        modifier = Modifier
            .padding(paddingValues)
    ) {
        NavHost(navController = navController, startDestination = Screen.Menu.route) {
            composable(Screen.Menu.route) {
                MenuScreen(
                    onNavigateBack = navController::navigateUp,
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    viewModel = menuViewModel
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    canNavigateBack = true,
                    onNavigateBack = navController::navigateUp,
                    onNavigateToOverview = { navController.navigate(Screen.Overview.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    viewModel = menuViewModel
                )
            }
            composable(Screen.Overview.route) {
                OverviewScreen(
                    onNavigateBack = navController::navigateUp,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateHome = {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Menu.route) {
                                inclusive = true
                            }
                        }
                    },
                    canNavigateBack = true,
                    menuViewModel = menuViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = navController::navigateUp
                )
            }
        }
    }
}