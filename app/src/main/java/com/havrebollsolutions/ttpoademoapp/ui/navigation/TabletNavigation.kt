package com.havrebollsolutions.ttpoademoapp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.havrebollsolutions.ttpoademoapp.ui.screens.SettingsScreen
import com.havrebollsolutions.ttpoademoapp.ui.screens.TabletLayout
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel


sealed class TabletScreen(
    val route: String
) {
    object TabletOverview : TabletScreen("tabletOverview")
    object Settings : TabletScreen("settings")
}

@Composable
fun TabletNavigation(
    menuViewModel: MenuViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {

    val detailNavController = rememberNavController()

    Column(
        modifier = Modifier
            .padding(paddingValues)
    ) {
        NavHost(navController = detailNavController, startDestination = TabletScreen.TabletOverview.route) {
            composable(TabletScreen.TabletOverview.route) {
                TabletLayout(
                    menuViewModel = menuViewModel,
                    onNavigateToSettings = { detailNavController.navigate(TabletScreen.Settings.route) }
                )
            }
            composable(TabletScreen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = detailNavController::navigateUp
                )
            }
        }
    }
}