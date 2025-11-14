package com.havrebollsolutions.ttpoademoapp.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem


// In SettingsScreen.kt

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityOptionsCompat
import coil3.compose.AsyncImage
import com.adyen.ipp.cardreader.api.ui.DeviceManagementActivity
import com.havrebollsolutions.ttpoademoapp.R
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import com.havrebollsolutions.ttpoademoapp.ui.components.AppBar
import com.havrebollsolutions.ttpoademoapp.ui.theme.TTPOADemoAppTheme
import com.havrebollsolutions.ttpoademoapp.viewmodel.ImageCategory
import com.havrebollsolutions.ttpoademoapp.viewmodel.SettingsUiState
import com.havrebollsolutions.ttpoademoapp.viewmodel.SettingsViewModel
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.config.ScannerConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // From SettingsViewModel
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = { /* Handle settings navigation */ },
                displaySettings = false,
                logoUri = settingsUiState.logotypeUriPath
            )
        }
    ) { paddingValues ->
        SettingsScreenContent(
            settingsUiState = settingsUiState,
            settingsViewModel = settingsViewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }


}

@Composable
fun SettingsScreenContent(
    settingsUiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AppLogoSelectionCard(
            onLogoSelected = settingsViewModel::updateImageUri,
            selectedImageUri = settingsUiState.logotypeUriPath
        )

        CurrencySelectionCard(
            selectedCurrency = settingsUiState.selectedCurrency,
            onCurrencySelected = settingsViewModel::updateCurrency,
            onExpandRequest = settingsViewModel::updateCurrencyDropdownMenuExpanded,
            onDismissRequest = settingsViewModel::updateCurrencyDropdownMenuExpanded,
            isDropdownExpanded = settingsUiState.currencyDropdownMenuExpanded
        )

        AdyenConfigurationCard(
            configEnabled = settingsUiState.adyenTtpConfigEnabled,
            enableAdyenTtpConfig = settingsViewModel::updateAdyenConfigEnabled,
            onAdyenConfigSave = settingsViewModel::saveAdyenConfig,
            merchantAccount = settingsUiState.adyenMerchantAccountTextField,
            store = settingsUiState.adyenStoreTextField,
            apiKey = settingsUiState.adyenApiKeyTextField,
            onMerchantAccountValueChange = {
                settingsViewModel.updateAdyenConfigTextFields(
                    merchantAccount = it
                )
            },
            onStoreValueChange = { settingsViewModel.updateAdyenConfigTextFields(store = it) },
            onApiKeyValueChange = { settingsViewModel.updateAdyenConfigTextFields(apiKey = it) },
            isQrScannerAvailable = settingsUiState.isQrScannerAvailable,
            onQrCodeScanned = settingsViewModel::handleAdyenConfigQrCodeResult,
            onClearSession = settingsViewModel::clearAdyenSession,
            launchQrScanner = settingsViewModel::launchQrScanner
        )

        // New Item Form Component
        NewItemFormCard(
            uiState = settingsUiState,
            onNameChange = { settingsViewModel.updateNewItemTextFields(name = it) },
            onDescChange = { settingsViewModel.updateNewItemTextFields(desc = it) },
            onPriceChange = { settingsViewModel.updateNewItemTextFields(price = it) },
            onVatRateChange = { settingsViewModel.updateNewItemTextFields(vatRate = it) },
            onQuantityChange = { settingsViewModel.updateNewItemTextFields(quantity = it) },
            onImageSelected = settingsViewModel::updateImageUri,
            onAddItem = {
                settingsViewModel.insertMenuItem()
                settingsViewModel.clearNewItemTextFields()
            }
        )

        // Existing Items List Component
        ExistingMenuItemsCard(
            menuItems = settingsUiState.menuItems,
            onClickMoveMenuItemUp = settingsViewModel::onClickMoveMenuItemUp,
            onClickMoveMenuItemDown = settingsViewModel::onClickMoveMenuItemDown,
            onDeleteItem = settingsViewModel::deleteMenuItem
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), // Row must fill the width
            horizontalArrangement = Arrangement.Center // Center the content within the Row
        ) {
            Text(
                text = stringResource(R.string.made_with_3_by_j_kisselgof_2025),
                textAlign = TextAlign.Center,
                // Modifier.fillMaxWidth() is often still a good idea here
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppLogoSelectionCard(
    onLogoSelected: (Uri?, ImageCategory) -> Unit,
    selectedImageUri: Uri? = null,
    modifier: Modifier = Modifier
) {

    // LAUNCHER: Launches the image picker when the row is clicked.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // This is the callback when the user selects or cancels the picker
        onLogoSelected(uri, ImageCategory.LOGO_IMAGE)
    }

    Card(
        modifier = modifier
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .size(120.dp)
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.app_logo))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Define the default/fallback logo painter once
                val defaultLogoPainter = painterResource(R.drawable.no_logo_curly)
                // Use AsyncImage as the only element, forcing it to handle all states
                AsyncImage(
                    // 1. Build a custom request to handle null/empty models explicitly
                    model = if (selectedImageUri.toString().isBlank()) {
                        // When the URI is null or blank, pass the resource ID instead
                        R.drawable.no_logo_curly
                    } else {
                        // Otherwise, pass the actual URI
                        selectedImageUri
                    },

                    contentDescription = "App Logo",

                    // 2. Fallback and Placeholder now use the default logo
                    placeholder = defaultLogoPainter, // Shown while loading
                    fallback = defaultLogoPainter,    // Shown if 'model' is null/empty
                    error = defaultLogoPainter,       // Shown if loading failed

                    contentScale = ContentScale.Fit,

                    // 3. Critically, set a fixed size on the modifier
                    modifier = Modifier
//                        .size(LOGO_SIZE) // Forces a stable size, preventing flicker
                        .padding(end = 8.dp)
                )

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Currency",
                    Modifier.clickable(
                        onClick = {
                            // Launch the image picker, specifically asking for images
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                )
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Edit Currency",
                    Modifier.clickable(
                        onClick = {
                            onLogoSelected(null, ImageCategory.LOGO_IMAGE)
                        }
                    )
                )
            }
        }
    }
}

@Composable
fun CurrencySelectionCard(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    onExpandRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    isDropdownExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.selected_currency))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandRequest() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.CenterEnd) {
                    // Display the currently selected currency code
                    Text(
                        text = selectedCurrency.isoCode,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = onDismissRequest
                    ) {
                        // Iterate over all values in the Currency enum
                        Currency.entries.forEach { currency ->
                            DropdownMenuItem(
                                text = {
                                    Text("${currency.isoCode} (${currency.symbol})")
                                },
                                onClick = {
                                    onCurrencySelected(currency)
                                    onDismissRequest()
                                } // Report the selected currency
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Currency"
                )
            }
        }
    }
}

@OptIn(
    DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun AdyenConfigurationCard(
    configEnabled: Boolean,
    enableAdyenTtpConfig: () -> Unit,
    onAdyenConfigSave: () -> Unit,
    merchantAccount: String,
    store: String,
    apiKey: String,
    isQrScannerAvailable: Boolean,
    onQrCodeScanned: (QRResult) -> Unit,
    launchQrScanner: (ManagedActivityResultLauncher<ScannerConfig, QRResult>) -> Unit,
    onClearSession: () -> Unit,
    onMerchantAccountValueChange: (String) -> Unit,
    onStoreValueChange: (String) -> Unit,
    onApiKeyValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.adyen_ttpoa_configuration),
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            // Adyen Configuration Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row {
                    Text(
                        text = stringResource(R.string.configuration_data),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                onAdyenConfigSave()
                                enableAdyenTtpConfig()
                            },
                            enabled = configEnabled,
                            modifier = Modifier
                                .height(24.dp),
                        ) { }
                        Text(
                            text = "Save",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Currency",
                        modifier = Modifier
                            .clickable(
                                onClick = enableAdyenTtpConfig
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Merchant Account Field
                OutlinedTextField(
                    value = merchantAccount,
                    onValueChange = onMerchantAccountValueChange,
                    label = { Text(text = stringResource(R.string.merchant_account)) },
                    maxLines = 1,
                    enabled = configEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                // Store Field
                OutlinedTextField(
                    value = store,
                    onValueChange = onStoreValueChange,
                    label = { Text(text = stringResource(R.string.store)) },
                    maxLines = 1,
                    enabled = configEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
                // API key Field
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyValueChange,
                    label = { Text(text = "API Key") },
                    maxLines = 1,
                    enabled = configEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    // 1. Set the visual transformation to hide text with dots
                    visualTransformation = PasswordVisualTransformation(),
                    // 2. Set the keyboard to provide password-specific suggestions/features
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(12.dp))
                GetAdyenConfigurationFromQRCode(
                    enabled = configEnabled,
                    isQrScannerAvailable = isQrScannerAvailable,
                    onQrCodeScanned = onQrCodeScanned,
                    launchQrScanner = launchQrScanner
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.mobile_terminal_settings),
                )
                Button(
                    onClick = {
                        // Launching from Compose

                        (context as? Activity)?.let { activity ->
                            DeviceManagementActivity.start(activity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Settings")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.clear_adyen_session),
                )
                Button(
                    onClick = onClearSession,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_session_button))
                }
            }
        }
    }
}

@Composable
fun GetAdyenConfigurationFromQRCode(
    enabled: Boolean,
    isQrScannerAvailable: Boolean,
    onQrCodeScanned: (QRResult) -> Unit,
    launchQrScanner: (ManagedActivityResultLauncher<ScannerConfig, QRResult>) -> Unit,
    modifier: Modifier = Modifier
) {
    val scanQRCodeLauncher = rememberLauncherForActivityResult(ScanCustomCode()) { result ->
        // Handle the result of the scan
        onQrCodeScanned(result)

    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        // Check if the QR scanner is available and display QR scanning option if true
        if (isQrScannerAvailable) {
            Button(
                onClick = {
                    launchQrScanner(scanQRCodeLauncher)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                enabled = enabled
            ) {
                Text(text = "Configure using QR code")
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                )
            }
        }
    }
}

@Composable
fun MenuItemThumbnailPicker(
    // This is the callback to save the URI to the ViewModel/Repository
    onThumbnailSelected: (Uri?, ImageCategory) -> Unit,
    selectedImageUri: Uri? = null
) {

    // LAUNCHER: Launches the image picker when the button is clicked.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // This is the callback when the user selects or cancels the picker
        onThumbnailSelected(uri, ImageCategory.MENU_IMAGE)
    }

    Column(
        modifier = Modifier
            .widthIn(min = 500.dp, max = 500.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- A. Display Area (The Thumbnail) ---
        Card(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (selectedImageUri != null) {
                // Coil's AsyncImage is perfect for loading URI/URL content
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Menu item thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Ensures the image fills the area
                )
            } else {
                // Placeholder when no image is selected
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No Image")
                }
            }
        }

        // --- B. The Button to Open the Picker ---
        Button(
            onClick = {
                // Launch the image picker, specifically asking for images
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.5f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(if (selectedImageUri != null) "Change Thumbnail" else "Add Thumbnail")
        }
    }
}

@Composable
fun NewItemFormCard(
    uiState: SettingsUiState,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onVatRateChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onImageSelected: (Uri?, ImageCategory) -> Unit,
    onAddItem: () -> Unit
) {
    // Get Focus Manager to clear focus
    val focusManager = LocalFocusManager.current
    // Get Keyboard Controller to explicitly hide the keyboard (optional, but robust)
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardActions = KeyboardActions(
        onDone = {
            // Clear focus when the keyboard is done
            focusManager.clearFocus()
            // Hide the keyboard when done
            keyboardController?.hide()
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.add_new_item),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Name Field
            OutlinedTextField(
                value = uiState.newItemName,
                onValueChange = onNameChange,
                label = { Text("Item Name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            // Description Field
            OutlinedTextField(
                value = uiState.newItemDesc,
                onValueChange = onDescChange,
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            // Price Field
            OutlinedTextField(
                value = uiState.newItemPrice,
                onValueChange = onPriceChange,
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            // ... (Other fields follow the same pattern) ...
            OutlinedTextField(
                value = uiState.newItemVatRate,
                onValueChange = onVatRateChange,
                label = { Text("VAT Rate") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.newItemQuantity,
                onValueChange = onQuantityChange,
                label = { Text("Quantity in Stock") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = keyboardActions,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Image Picker Composable (assuming it was updated to accept selectedImageUri)
            MenuItemThumbnailPicker(
                onThumbnailSelected = onImageSelected,
                selectedImageUri = uiState.newItemImageUri
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_item))
            }
        }
    }
}

@Composable
fun ExistingMenuItemsCard(
    menuItems: List<MenuItem>,
    onClickMoveMenuItemUp: (MenuItem) -> Unit,
    onClickMoveMenuItemDown: (MenuItem) -> Unit,
    onDeleteItem: (MenuItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.existing_items),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(
                    menuItems,
                    key = { it.id }
                ) { item ->
                    MenuItemRow(
                        item = item,
                        onClickMoveMenuItemUp = { onClickMoveMenuItemUp(item) },
                        onClickMoveMenuItemDown = { onClickMoveMenuItemDown(item) },
                        onDelete = { onDeleteItem(item) },
                        modifier = Modifier
                            .animateItem(
                            fadeInSpec = tween(durationMillis = 250),
                            fadeOutSpec = tween(durationMillis = 100),
                            )
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// Further extraction for list item reusability
@Composable
fun MenuItemRow(
    item: MenuItem,
    onClickMoveMenuItemUp: () -> Unit,
    onClickMoveMenuItemDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Row {
            IconButton(onClick = onClickMoveMenuItemUp) {
                Icon(
                    imageVector = Icons.Default.MoveUp,
                    contentDescription = stringResource(R.string.move_up),
                )
            }
            IconButton(onClick = onClickMoveMenuItemDown) {
                Icon(
                    imageVector = Icons.Default.MoveDown,
                    contentDescription = stringResource(R.string.move_down),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${item.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppLogoSelectionCardPreview() {
    TTPOADemoAppTheme {
        AppLogoSelectionCard(onLogoSelected = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun ThumbnailPickerPreview() {
    TTPOADemoAppTheme {
        MenuItemThumbnailPicker(onThumbnailSelected = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencySelectionCardPreview() {
    TTPOADemoAppTheme {
        CurrencySelectionCard(
            selectedCurrency = Currency.SEK,
            onCurrencySelected = {},
            onExpandRequest = {},
            onDismissRequest = {},
            isDropdownExpanded = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdyenTtpConfigurationCardPreview() {
    TTPOADemoAppTheme {
        AdyenConfigurationCard(
            configEnabled = false,
            enableAdyenTtpConfig = {},
            merchantAccount = "",
            store = "",
            apiKey = "",
            onMerchantAccountValueChange = {},
            onStoreValueChange = {},
            onApiKeyValueChange = {},
            onAdyenConfigSave = {},
            isQrScannerAvailable = true,
            onQrCodeScanned = {},
            onClearSession = {},
            launchQrScanner = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GetAdyenConfigurationFromQRCodePreview() {
    TTPOADemoAppTheme {
        GetAdyenConfigurationFromQRCode(
            enabled = false,
            isQrScannerAvailable = true,
            onQrCodeScanned = {},
            launchQrScanner = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuItemRowPreview() {
    TTPOADemoAppTheme {
        MenuItemRow(
            item = MenuItem(
                name = "Example Item",
                description = "This is an example item.",
                price = 19.99,
                vatRate = 25.0,
                quantityInStock = 10,
                imagePath = null,
                orderIndex = 0
            ),
            onClickMoveMenuItemUp = {},
            onClickMoveMenuItemDown = {},
            onDelete = {}
        )
    }
}