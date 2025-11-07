package com.havrebollsolutions.ttpoademoapp.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.havrebollsolutions.ttpoademoapp.R
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppBar(
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    displaySettings: Boolean = true,
    logoUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    // Define the default/fallback logo painter once
    val defaultLogoPainter = painterResource(R.drawable.no_logo_curly)

    // Define a stable size for the logo slot
    val LOGO_SIZE = 120.dp

    CenterAlignedTopAppBar(
        title = {
            // Use AsyncImage as the only element, forcing it to handle all states
            AsyncImage(
                // 1. Build a custom request to handle null/empty models explicitly
                model = if (logoUri.toString().isBlank()) {
                    // When the URI is null or blank, pass the resource ID instead
                    R.drawable.no_logo_curly
                } else {
                    // Otherwise, pass the actual URI
                    logoUri
                },

                contentDescription = "App Logo",

                // 2. Fallback and Placeholder now use the default logo
                placeholder = defaultLogoPainter, // Shown while loading
                fallback = defaultLogoPainter,    // Shown if 'model' is null/empty
                error = defaultLogoPainter,       // Shown if loading failed

                contentScale = ContentScale.Fit,

                // 3. Critically, set a fixed size on the modifier
                modifier = Modifier
                    .size(LOGO_SIZE) // Forces a stable size, preventing flicker
                    .padding(8.dp)
            )
        },
//       { Text("TTPOA Demo App") },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }

        },
        actions = {
            if (displaySettings) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    )
}