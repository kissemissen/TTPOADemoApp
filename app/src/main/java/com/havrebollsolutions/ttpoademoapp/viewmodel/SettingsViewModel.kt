package com.havrebollsolutions.ttpoademoapp.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.ipp.api.InPersonPayments
import com.adyen.ipp.cardreader.api.ui.DeviceManagementActivity
import com.google.gson.Gson
import com.havrebollsolutions.ttpoademoapp.DeviceUtils
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.models.AdyenConfig
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import com.havrebollsolutions.ttpoademoapp.data.repository.MenuItemRepository
import com.havrebollsolutions.ttpoademoapp.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.config.ScannerConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class ImageCategory(
    val imageType: String
) {
    LOGO_IMAGE("logo"),
    MENU_IMAGE("menu")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val menuItemRepository: MenuItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context // Inject app context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(Currency.SEK))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            menuItemRepository.getAllMenuItems().collect { items ->
                _uiState.value = _uiState.value.copy(menuItems = items)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.logotypeUriPath.collect { uriPath ->
                _uiState.value = _uiState.value.copy(logotypeUriPath = uriPath?.toUri())
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.selectedCurrency.collect { currency ->
                _uiState.value = _uiState.value.copy(selectedCurrency = currency)
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.adyenConfig.collect { config ->
                if (config != null) {
                    _uiState.value = _uiState.value.copy(
                        adyenMerchantAccountTextField = config.merchantAccount,
                        adyenStoreTextField = config.store,
                        adyenApiKeyTextField = config.apiKey
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        adyenMerchantAccountTextField = "",
                        adyenStoreTextField = "",
                        adyenApiKeyTextField = ""
                    )
                }
            }
        }

        // Check camera availability once
        val cameraStatus = DeviceUtils.getAvailableCameras(context)
        val scannerAvailable = cameraStatus.hasRearCamera || cameraStatus.hasFrontCamera
        // Priority logic: Rear camera first, otherwise front camera
        val useFront = !cameraStatus.hasRearCamera && cameraStatus.hasFrontCamera
        // Update the UI state with the camera availability
        _uiState.update { currentState ->
            currentState.copy(
                isQrScannerAvailable = scannerAvailable,
                useFrontCamera = useFront
            )
        }
    }

    fun updateLogotype(imageUri: Uri?) {
        viewModelScope.launch {
            userPreferencesRepository.saveLogotypeUriPath(imageUri.toString())
        }
    }

    fun updateCurrencyDropdownMenuExpanded() {
        _uiState.update { currentState ->
            currentState.copy(currencyDropdownMenuExpanded = !_uiState.value.currencyDropdownMenuExpanded)
        }
    }

    fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedCurrency(currency)
        }
    }

    /**
     * Saves the Adyen configuration to the repository.
     */
    fun saveAdyenConfig() {
        viewModelScope.launch {
            userPreferencesRepository.saveAdyenConfig(
                AdyenConfig(
                    merchantAccount = _uiState.value.adyenMerchantAccountTextField,
                    store = _uiState.value.adyenStoreTextField,
                    apiKey = _uiState.value.adyenApiKeyTextField
                )
            )
        }
    }

    fun updateAdyenConfigEnabled() {
        _uiState.update { currentState ->
            currentState.copy(
                adyenTtpConfigEnabled = !_uiState.value.adyenTtpConfigEnabled
            )
        }
    }

    fun updateAdyenConfigTextFields(
        merchantAccount: String = uiState.value.adyenMerchantAccountTextField,
        store: String = uiState.value.adyenStoreTextField,
        apiKey: String = uiState.value.adyenApiKeyTextField
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                adyenMerchantAccountTextField = merchantAccount,
                adyenStoreTextField = store,
                adyenApiKeyTextField = apiKey
            )
        }
    }

    /**
     * Clears the Adyen session.
     */
    fun clearAdyenSession() {
        viewModelScope.launch {
            InPersonPayments.clearSession()
        }
    }


    /**
     * Launches the QR code scanner.
     * @param scanQRCodeLauncher The launcher for the QR code scanner.
     */
    fun launchQrScanner(scanQRCodeLauncher: ManagedActivityResultLauncher<ScannerConfig, QRResult>) {
        // 1. Logic lives entirely in the ViewModel
        val scannerConfig = ScannerConfig.build {
            // Use the state determined during initialization
            setUseFrontCamera(_uiState.value.useFrontCamera)
            setShowCloseButton(true)
        }

        // 2. ViewModel sends the event (the command + data) to the UI
        viewModelScope.launch {
            scanQRCodeLauncher.launch(input = scannerConfig)
        }
    }

    /**
     * Handles the result of a QR code scan to populate the Adyen configuration fields.
     * @param qrResult The result of the QR code scan.
     */
    fun handleAdyenConfigQrCodeResult(qrResult: QRResult) {
        when(qrResult) {
            is QRResult.QRSuccess -> {
                try {
                    // 1. Parse the JSON string into the AdyenConfig data class
                    val config = Gson().fromJson(qrResult.content.rawValue, AdyenConfig::class.java)
                    Log.d("SettingsViewModel", "Parsed AdyenConfig: ${qrResult.content.rawValue}")

                    // 2. Validate essential fields (optional but recommended)
                    if (config.merchantAccount.isBlank() || config.apiKey.isBlank()) {
                        throw IllegalArgumentException("QR code missing essential configuration data.")
                    }

                    // 3. Update the UI state with the parsed data
                    updateAdyenConfigTextFields(
                        merchantAccount = config.merchantAccount,
                        store = config.store,
                        apiKey = config.apiKey
                    )
                } catch (e: Exception) {
                    // Handle parsing errors or missing data
                    Log.e("SettingsViewModel", "Failed to process QR code data", e)
                }
            }

            is QRResult.QRError -> {
                // Handle QR scan errors
                Log.e("SettingsViewModel", "QR scan error: ${qrResult.exception.message}")
            }

            is QRResult.QRUserCanceled -> {
                // Handle user canceling the QR scan
                Log.d("SettingsViewModel", "QR scan canceled by user")
            }

            is QRResult.QRMissingPermission -> {
                // Handle missing camera permission
                Log.e("SettingsViewModel", "Missing camera permission")
            }
        }

    }

    /**
     * Updates the uiState with new value in text fields to insert new menu items
     */
    fun updateNewItemTextFields(
        name: String = uiState.value.newItemName,
        desc: String = uiState.value.newItemDesc,
        price: String = uiState.value.newItemPrice,
        vatRate: String = uiState.value.newItemVatRate,
        quantity: String = uiState.value.newItemQuantity,
        imageUri: Uri? = uiState.value.newItemImageUri
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                newItemName = name,
                newItemDesc = desc,
                newItemPrice = price,
                newItemVatRate = vatRate,
                newItemQuantity = quantity,
                newItemImageUri = imageUri
            )
        }
    }

    fun clearNewItemTextFields() {
        updateNewItemTextFields(
            name = "",
            desc = "",
            price = "",
            vatRate = "",
            quantity = "",
            imageUri = null
        )
    }

    fun updateImageUri(uri: Uri?, imageCategory: ImageCategory) {
        if (uri != null) {
            // Get the contentResolver from Context
            val contextResolver = context.contentResolver

            // **PERSIST READ PERMISSION**
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contextResolver.takePersistableUriPermission(uri, flag)
            } catch (e: SecurityException) {
                Log.e("SettingsViewModel", "Failed to persist URI permission: ${e.message}")
            }
            // Store the URI's string representation in the ViewModel state
            when (imageCategory) {
                ImageCategory.LOGO_IMAGE -> updateLogotype(imageUri = uri)
                ImageCategory.MENU_IMAGE -> updateNewItemTextFields(imageUri = uri)
            }
        } else {
            Log.d("SettingsViewmodel", "Logo URI: $uri")
            when (imageCategory) {
                ImageCategory.LOGO_IMAGE -> updateLogotype(imageUri = null)
                ImageCategory.MENU_IMAGE -> updateNewItemTextFields(imageUri = null)
            }
        }
    }

    /**
     * Inserts a new menu item into the database.
     */
    fun insertMenuItem() {
        viewModelScope.launch {
            menuItemRepository.insertMenuItem(
                MenuItem(
                    name = _uiState.value.newItemName,
                    description = _uiState.value.newItemDesc,
                    price = _uiState.value.newItemPrice.toDoubleOrNull() ?: 0.0,
                    vatRate = _uiState.value.newItemVatRate.toDoubleOrNull() ?: 0.0,
                    quantityInStock = _uiState.value.newItemQuantity.toIntOrNull() ?: 0,
                    imagePath = _uiState.value.newItemImageUri.toString()
                )
            )
        }
    }

    /**
     * Deletes a menu item from the database.
     */
    fun deleteMenuItem(menuItem: MenuItem) {
        viewModelScope.launch {
            menuItemRepository.deleteMenuItem(menuItem)
        }
    }

    /**
     * Warms up the Adyen SDK.
     */
    fun warmUpAdyenSDK() {
        viewModelScope.launch {
            val result = InPersonPayments.warmUp()
            Log.d("MenuViewModel", "${result.getOrThrow()}")
        }
    }
}

/**
 * Ui State for the Settings screen
 */
data class SettingsUiState(
    val selectedCurrency: Currency,
    val logotypeUriPath: Uri? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val currencyDropdownMenuExpanded: Boolean = false,
    val isQrScannerAvailable: Boolean = false,
    val useFrontCamera: Boolean = false,
    val adyenTtpConfigEnabled: Boolean = false,
    val adyenMerchantAccountTextField: String = "",
    val adyenStoreTextField: String = "",
    val adyenApiKeyTextField: String = "",
    val newItemName: String = "",
    val newItemDesc: String = "",
    val newItemPrice: String = "",
    val newItemVatRate: String = "",
    val newItemQuantity: String = "",
    val newItemImageUri: Uri? = null
)