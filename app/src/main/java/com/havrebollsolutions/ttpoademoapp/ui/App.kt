package com.havrebollsolutions.ttpoademoapp.ui

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Expanded
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Companion.Medium
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.havrebollsolutions.ttpoademoapp.ui.navigation.AppNavigation
import com.havrebollsolutions.ttpoademoapp.ui.navigation.TabletNavigation
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    val menuViewModel: MenuViewModel = hiltViewModel()
    val navController = rememberNavController()

    when (windowSizeClass.widthSizeClass) {
        Compact -> AppNavigation(
            menuViewModel = menuViewModel,
            navController = navController
        )

        Medium, Expanded -> TabletNavigation(
            menuViewModel = menuViewModel
        )
        //TabletLayout(menuViewModel = menuViewModel)
    }

}