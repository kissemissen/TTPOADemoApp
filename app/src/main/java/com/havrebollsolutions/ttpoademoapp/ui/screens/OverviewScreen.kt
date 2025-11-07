package com.havrebollsolutions.ttpoademoapp.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.adyen.ipp.api.payment.PaymentResultContract
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import com.havrebollsolutions.ttpoademoapp.ui.components.AppBar
import com.havrebollsolutions.ttpoademoapp.ui.theme.TTPOADemoAppTheme
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartItem
import com.havrebollsolutions.ttpoademoapp.viewmodel.DevicePaymentDialog
import com.havrebollsolutions.ttpoademoapp.viewmodel.DevicePaymentViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.PaymentOption


@Composable
fun OverviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    isTabletLayout: Boolean = false,
    canNavigateBack: Boolean = false,
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    devicePaymentViewModel: DevicePaymentViewModel = hiltViewModel()
) {
    // From CartViewModel
    val cartUiState by cartViewModel.uiState.collectAsState()

    // from MenuViewModel
    val cartItems by menuViewModel.cartItems.collectAsState()
    val totalAmount by menuViewModel.cartTotalAmount.collectAsState()
    val selectedCurrency by menuViewModel.selectedCurrency.collectAsState()

    // from DevicePaymentViewModel
    val devicePaymentUiState by devicePaymentViewModel.uiState.collectAsState()


    if (cartUiState.isCartPaid) {
        onNavigateHome()
    }

    // Define launcher
    val launcher = rememberLauncherForActivityResult(PaymentResultContract()) { result ->
        Log.d("OverviewScreen", "Payment result: ${result.getOrThrow().data}")
        // Important: Side effect logic lives here
        // If payment is successful, clear cart and navigate
        result.fold(
            onSuccess = { paymentResult ->
                val nexoResponse = cartViewModel.decodeBase64NexoResponse(paymentResult.data)
                cartViewModel.checkout(
                    nexoResponse = nexoResponse,
                    cartItems = cartItems,
                    paymentMethod = cartUiState.selectedPaymentOption?.name ?: "",
                    clearCart = menuViewModel::clearCart
                )
            },
            onFailure = { error ->
                Log.d(
                    "OverviewScreen",
                    "An error occurred when processing over TTP. See ${error.localizedMessage}"
                )
            }
        )
//        if (result.isSuccess) {
//            onNavigateBack()
//        }
    }

    // Define event handler (hoist payment lambda)
    // Use when "Complete button clicked"
    val onInitiatePayment = {
        // Call the ViewModel, passing the execution context (launcher and amount)
        cartViewModel.updatePaymentInProgress(true)
        if (cartUiState.selectedPaymentOption == PaymentOption.Terminal) {
            devicePaymentViewModel.updateConnectedDevices()
        } else {
            cartViewModel.initiatePayment(
                amount = totalAmount,
                launcher = launcher
            )
        }
    }

    // Define event handler (hoist payment lambda)
    // Used in Device Payment Screen Module
    val onCancelDevicePayment = {
        cartViewModel.updatePaymentInProgress(false)
        devicePaymentViewModel.cancelDevicePyment()
    }

    // Define event handler (hoist payment lambda)
    // Used in Device Payment Screen Module
    val onDismiss = {
        cartViewModel.checkout(
            nexoResponse = devicePaymentUiState.nexoResponse,
            cartItems = cartItems,
            paymentMethod = cartUiState.selectedPaymentOption?.name ?: "",
            clearCart = menuViewModel::clearCart
        )
        devicePaymentViewModel.updateDevicePaymentDialog(DevicePaymentDialog.SelectDeviceDialog)
        devicePaymentViewModel.cancelDevicePyment()
    }

    // Define event handler (hoist payment lambda)
    // Used in Device Payment Screen Module
    // Used to initate payment on device
    val onInitiatePaymentOnDevice = {
        devicePaymentViewModel.initatePaymentOnDevice(totalAmount)
    }

    Scaffold(
        topBar = {
            if (!isTabletLayout) {
                AppBar(
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = onNavigateBack,
                    onNavigateToSettings = onNavigateToSettings,
                    logoUri = cartUiState.logotypeUriPath
                )
            }


        }
    ) { paddingValues ->
        OverviewScreenContent(
            cartItems = cartItems,
            totalAmount = totalAmount,
            selectedCurrency = selectedCurrency,
            selectedPaymentOption = cartUiState.selectedPaymentOption,
            onSelectedPaymentOption = cartViewModel::updateSelectedPaymentOption,
            isPaymentInProgress = cartUiState.paymentInProgess,
            onCompletePurchase = onInitiatePayment,
            connectedDevices = devicePaymentUiState.connectedDevices,
            onDeviceSelected = devicePaymentViewModel::updateSelectedDevice,
            selectedDevice = devicePaymentUiState.selectedDevice,
            devicePaymentDialog = devicePaymentUiState.devicePaymentDialog,
            onCancelDevicePayment = onCancelDevicePayment,
            onInitiatePaymentOnDevice = onInitiatePaymentOnDevice,
            onAbortPaymentOnDevice = devicePaymentViewModel::abortPaymentOnDevice,
            isPaymentSuccessful = devicePaymentUiState.isPaymentSuccessful,
            isLoadingInProgress = devicePaymentUiState.loadingInProgress,
            isTabletLayout = isTabletLayout,
            onDismiss = onDismiss,
            modifier = modifier,
            paddingValues = paddingValues
        )
    }
}

@Composable
fun OverviewScreenContent(
    cartItems: List<CartItem>,
    totalAmount: Double,
    selectedCurrency: Currency,
    selectedPaymentOption: PaymentOption?,
    onSelectedPaymentOption: (PaymentOption) -> Unit,
    isPaymentInProgress: Boolean,
    onCompletePurchase: () -> Unit,
    connectedDevices: List<String>,
    onDeviceSelected: (String) -> Unit,
    devicePaymentDialog: DevicePaymentDialog,
    onCancelDevicePayment: () -> Unit,
    onInitiatePaymentOnDevice: () -> Unit,
    onAbortPaymentOnDevice: () -> Unit,
    onDismiss: () -> Unit,
    isPaymentSuccessful: Boolean,
    isLoadingInProgress: Boolean,
    modifier: Modifier = Modifier,
    selectedDevice: String? = null,
    isTabletLayout: Boolean = false,
    paddingValues: PaddingValues = PaddingValues()
) {
//    val context = LocalContext.current
//    val launcher = rememberLauncherForActivityResult(PaymentResultContract()) { result ->
//        Log.d("SettingsScreen", "Payment result: ${result.getOrThrow().data}")
//        onNavigateBack()
//    }

    Box(modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (isPaymentInProgress) {
                        Modifier.blur(5.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isTabletLayout) {
                    Text(
                        text = "Order Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(cartItems) { item ->
                            CartItemRow(selectedCurrency = selectedCurrency, item = item)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Total: ${selectedCurrency.symbol} ${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PaymentOptionChip(
                        option = PaymentOption.TapToPay,
                        isSelected = selectedPaymentOption == PaymentOption.TapToPay,
                        onClick = { onSelectedPaymentOption(PaymentOption.TapToPay) }
                    )
                    PaymentOptionChip(
                        option = PaymentOption.NYC1,
                        isSelected = selectedPaymentOption == PaymentOption.NYC1,
                        onClick = { onSelectedPaymentOption(PaymentOption.NYC1) }
                    )
                    PaymentOptionChip(
                        option = PaymentOption.Terminal,
                        isSelected = selectedPaymentOption == PaymentOption.Terminal,
                        onClick = { onSelectedPaymentOption(PaymentOption.Terminal) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedPaymentOption != null) {
                            onCompletePurchase()
                        }
                    },
                    enabled = selectedPaymentOption != null && !isPaymentInProgress && cartItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Complete Purchase")
                    if (isPaymentInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
    if (!isTabletLayout) {
        if (isPaymentInProgress && selectedPaymentOption == PaymentOption.Terminal) {
            DevicePaymentScreen(
                connectedDevices = connectedDevices,
                onCancelDevicePayment = onCancelDevicePayment,
                onDeviceSelected = onDeviceSelected,
                selectedDevice = selectedDevice,
                devicePaymentDialog = devicePaymentDialog,
                onInitiatePaymentOnDevice = onInitiatePaymentOnDevice,
                onAbortPaymentOnDevice = onAbortPaymentOnDevice,
                onDismiss = onDismiss,
                isPaymentSuccessful = isPaymentSuccessful,
                isLoadingInProgress = isLoadingInProgress
            )
        }
    }
}

@Composable
fun CartItemRow(selectedCurrency: Currency, item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${item.quantity}x ${item.menuItem.name}")
        Text("${selectedCurrency.symbol} ${"%.2f".format(item.menuItem.price * item.quantity)}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentOptionChip(option: PaymentOption, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(option.name) }
    )
}

@Composable
@Preview(showBackground = true)
fun OverviewScreenContentPreview() {
    TTPOADemoAppTheme {
        val mockMenuItem = MenuItem(
            id = 1,
            name = "Test Item",
            description = "Test Description",
            price = 10.0,
            vatRate = 10.0,
            quantityInStock = 10,
            imagePath = null
        )
        val mockCartItem = CartItem(mockMenuItem, 10)
        val mockCartItems = listOf(mockCartItem)
        OverviewScreenContent(
            cartItems = mockCartItems,
            totalAmount = mockCartItems.sumOf { it.menuItem.price * it.quantity },
            selectedCurrency = Currency.SEK,
            selectedPaymentOption = null,
            onSelectedPaymentOption = {},
            onCompletePurchase = {},
            connectedDevices = listOf("Device 1", "Device 2", "Device 3"),
            onDeviceSelected = {},
            selectedDevice = null,
            isPaymentInProgress = false,
            devicePaymentDialog = DevicePaymentDialog.SelectDeviceDialog,
            onCancelDevicePayment = {},
            isPaymentSuccessful = false,
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            isLoadingInProgress = false,
            onDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun OverviewScreenContentTerminalPaymentPreview() {
    TTPOADemoAppTheme {
        val mockMenuItem = MenuItem(
            id = 1,
            name = "Test Item",
            description = "Test Description",
            price = 10.0,
            vatRate = 10.0,
            quantityInStock = 10,
            imagePath = null
        )
        val mockCartItem = CartItem(mockMenuItem, 10)
        val mockCartItems = listOf(mockCartItem)
        OverviewScreenContent(
            cartItems = mockCartItems,
            totalAmount = mockCartItems.sumOf { it.menuItem.price * it.quantity },
            selectedCurrency = Currency.SEK,
            selectedPaymentOption = PaymentOption.Terminal,
            onSelectedPaymentOption = {},
            onCompletePurchase = {},
            selectedDevice = "Device 1",
            onDeviceSelected = {},
            isPaymentInProgress = true,
            connectedDevices = listOf("Device 1", "Device 2", "Device 3"),
            devicePaymentDialog = DevicePaymentDialog.SelectDeviceDialog,
            onCancelDevicePayment = {},
            isPaymentSuccessful = false,
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            isLoadingInProgress = false,
            onDismiss = {}
        )
    }
}