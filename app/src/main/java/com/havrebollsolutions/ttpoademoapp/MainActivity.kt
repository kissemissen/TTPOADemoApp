package com.havrebollsolutions.ttpoademoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.adyen.ipp.api.InPersonPayments
import com.havrebollsolutions.ttpoademoapp.di.service.AdyenAuthenticationService
import com.havrebollsolutions.ttpoademoapp.ui.App
import com.havrebollsolutions.ttpoademoapp.ui.theme.TTPOADemoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TTPOADemoAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val windowSizeClass = calculateWindowSizeClass(this)
                    App(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
