package com.havrebollsolutions.ttpoademoapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.havrebollsolutions.ttpoademoapp.ui.components.AppBar
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.DevicePaymentDialog
import com.havrebollsolutions.ttpoademoapp.viewmodel.DevicePaymentViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel
import com.havrebollsolutions.ttpoademoapp.viewmodel.PaymentOption

@Composable
fun TabletLayout(
    onNavigateToSettings: () -> Unit,
    menuViewModel: MenuViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    devicePaymentViewModel: DevicePaymentViewModel = hiltViewModel()
) {

    val menuUiState by menuViewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val devicePaymentUiState by devicePaymentViewModel.uiState.collectAsState()

    // from MenuViewModel
    val cartItems by menuViewModel.cartItems.collectAsState()
    val totalAmount by menuViewModel.cartTotalAmount.collectAsState()

    // Define event handler (hoist payment lambda)
    val onCancelDevicePayment = {
        cartViewModel.updatePaymentInProgress(false)
        devicePaymentViewModel.cancelDevicePyment()
    }

    // Define event handler (hoist payment lambda)
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
    val onInitiatePaymentOnDevice = {
        devicePaymentViewModel.initatePaymentOnDevice(totalAmount)
    }



    Scaffold(
        topBar = {
            AppBar(
                canNavigateBack = false,
                onNavigateBack = { /* No navigation needed in tablet mode */ },
                onNavigateToSettings = onNavigateToSettings,
                logoUri = menuUiState.logotypeUriPath
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Menu Section
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(8.dp)
                ) {
                    // MenuScreen needs to be a self-contained unit with no navigation logic.
                    // We'll pass empty lambdas for navigation.
                    MenuScreen(
                        onNavigateBack = { /* No navigation needed in tablet mode */ },
                        onNavigateToCart = { /* No navigation needed in tablet mode */ },
                        onNavigateToSettings = { /* No navigation needed in tablet mode */ },
                        isTabletLayout = true,
                        viewModel = menuViewModel
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                VerticalDivider(
                    thickness = Dp.Hairline,
                    modifier = Modifier
                        .fillMaxHeight(fraction = 0.5f)
                        .align(alignment = Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Cart and Overview sections
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        // The CartScreen will show the shared cart state from MenuViewModel.
                        CartScreen(
                            onNavigateBack = { /* No navigation needed in tablet mode */ },
                            onNavigateToOverview = { /* No navigation needed in tablet mode */ },
                            onNavigateToSettings = { /* No navigation needed in tablet mode */ },
                            isTabletLayout = true,
                            viewModel = menuViewModel
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .height(200.dp)
                    ) {
                        // The OverviewScreen will use the shared cart and checkout logic.
                        OverviewScreen(
                            onNavigateBack = { /* No navigation needed in tablet mode */ },
                            onNavigateToSettings = { /* No navigation needed in tablet mode */ },
                            onNavigateHome = { /* No navigation needed in tablet mode */ },
                            isTabletLayout = true,
                            menuViewModel = menuViewModel,
                            cartViewModel = cartViewModel
                        )
                    }
                }
            }
            // Add Device Payment Here
            if (cartUiState.paymentInProgess && cartUiState.selectedPaymentOption == PaymentOption.Terminal) {
                DevicePaymentScreen(
                    isTabletLayout = true,
                    connectedDevices = devicePaymentUiState.connectedDevices,
                    onCancelDevicePayment = onCancelDevicePayment,
                    onDeviceSelected = devicePaymentViewModel::updateSelectedDevice,
                    selectedDevice = devicePaymentUiState.selectedDevice,
                    devicePaymentDialog = devicePaymentUiState.devicePaymentDialog,
                    onInitiatePaymentOnDevice = onInitiatePaymentOnDevice,
                    onAbortPaymentOnDevice = devicePaymentViewModel::abortPaymentOnDevice,
                    onDismiss = onDismiss,
                    isPaymentSuccessful = devicePaymentUiState.isPaymentSuccessful,
                    isLoadingInProgress = devicePaymentUiState.loadingInProgress
                )
            }
        }
    }
}