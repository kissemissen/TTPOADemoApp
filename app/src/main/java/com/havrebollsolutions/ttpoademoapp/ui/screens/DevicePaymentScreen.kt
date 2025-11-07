package com.havrebollsolutions.ttpoademoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.havrebollsolutions.ttpoademoapp.R
import com.havrebollsolutions.ttpoademoapp.ui.theme.TTPOADemoAppTheme
import com.havrebollsolutions.ttpoademoapp.viewmodel.DevicePaymentDialog

@Composable
fun DevicePaymentScreen(
    onDeviceSelected: (String) -> Unit,
    devicePaymentDialog: DevicePaymentDialog,
    onCancelDevicePayment: () -> Unit,
    onInitiatePaymentOnDevice: () -> Unit,
    onAbortPaymentOnDevice: () -> Unit,
    onDismiss: () -> Unit,
    isPaymentSuccessful: Boolean,
    isLoadingInProgress: Boolean,
    selectedDevice: String? = null,
    modifier: Modifier = Modifier,
    isTabletLayout: Boolean = false,
    connectedDevices: List<String> = emptyList()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            // CRITICAL: Intercepts and consumes all down/up events
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = if (isTabletLayout) {
                Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.6f)
                    .padding(20.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(20.dp)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            when (devicePaymentDialog) {
                DevicePaymentDialog.SelectDeviceDialog -> {
                    DeviceSelection(
                        onDeviceSelected = onDeviceSelected,
                        selectedDevice = selectedDevice,
                        connectedDevices = connectedDevices,
                        onInitiatePaymentOnDevice = onInitiatePaymentOnDevice,
                        onCancelDevicePayment = onCancelDevicePayment,
                        isLoadingInProgress = isLoadingInProgress
                    )
                }

                DevicePaymentDialog.DevicePaymentInProgressDialog -> {
                    DevicePaymentInProgress(
                        selectedDevice = selectedDevice ?: "",
                        onAbortPaymentOnDevice = onAbortPaymentOnDevice, // FIX THIS TOMORROW,

                    )
                }

                DevicePaymentDialog.DevicePaymentSuccessDialog -> {
                    DevicePaymentSuccess(
                        isPaymentSuccessful = isPaymentSuccessful,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceSelection(
    onDeviceSelected: (String) -> Unit,
    onInitiatePaymentOnDevice: () -> Unit,
    onCancelDevicePayment: () -> Unit,
    isLoadingInProgress: Boolean,
    modifier: Modifier = Modifier,
    connectedDevices: List<String> = emptyList(),
    selectedDevice: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.select_payment_device),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            // List connected devices
            if (connectedDevices.isEmpty()) {
                // If loading devices, show indicator
                if (isLoadingInProgress) {
                    Row {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(start = 8.dp)
                        )
                        Text(text = "Loading devices...")
                    }
                } else {
                    // If empty list, show no devices connected
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.no_connected_devices_found),
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(connectedDevices) { item ->
                        DeviceItem(
                            deviceId = item,
                            isSelected = item == selectedDevice,
                            onDeviceSelected = onDeviceSelected,
                        )
                    }
                }
            }
        }
        // Add padding/separator before the button (optional)
        Spacer(modifier = Modifier.height(16.dp))

        // Button to start payment on device
        Button(
            onClick = onInitiatePaymentOnDevice,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDevice != null
        ) {
            Text(stringResource(R.string.initiate_payment_on_device))
        }
        // Button to start payment on device
        Button(
            onClick = onCancelDevicePayment,
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                // BG color
                containerColor = MaterialTheme.colorScheme.error,
                // Text color
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(text = "Cancel Device Payment")
        }
    }
}

@Composable
fun DevicePaymentInProgress(
    selectedDevice: String,
    onAbortPaymentOnDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.device_payment_in_progress),
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = "Device ID: $selectedDevice")
        Spacer(modifier = Modifier.height(16.dp))
        // Display animated GIF
        AsyncImage(
            model = R.drawable.contactless__transparent,
            contentDescription = "Device Payment In Progress Animation",
            modifier = Modifier
                .size(200.dp)
                .weight(1f)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button to start payment on device
        Button(
            onClick = onAbortPaymentOnDevice,
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                // BG color
                containerColor = MaterialTheme.colorScheme.error,
                // Text color
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.abort_device_payment))
        }
    }
}

@Composable
fun DevicePaymentSuccess(
    isPaymentSuccessful: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display animated GIF
            AsyncImage(
                model = if (isPaymentSuccessful) R.drawable.checkmark else R.drawable.cross_circle,
                contentDescription = "Device Payment In Progress Animation",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isPaymentSuccessful) {
                Text(
                    text = stringResource(R.string.payment_successful),
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                Text(
                    text = stringResource(R.string.payment_failed),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Button to start payment on device
        Button(
            onClick = onDismiss,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text("Dismiss")
        }
    }
}

@Composable
fun DeviceItem(
    deviceId: String,
    onDeviceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {

    // 1. Define visual properties based on state
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer // Highlight background
    } else {
        MaterialTheme.colorScheme.surface // Default background
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer // Highlight text/icon color
    } else {
        MaterialTheme.colorScheme.onSurface // Default text/icon color
    }

    val elevation = if (isSelected) {
        CardDefaults.cardElevation(defaultElevation = 8.dp) // Lift the card
    } else {
        CardDefaults.cardElevation(defaultElevation = 1.dp) // Lower/standard elevation
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        // 2. Apply the dynamic colors and elevation
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = elevation,
        onClick = { onDeviceSelected(deviceId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.pos_medium),
                contentDescription = "Device",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = deviceId,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicePaymentScreenPreview() {
    TTPOADemoAppTheme {
        DevicePaymentScreen(
            connectedDevices = listOf("Device 1", "Device 2", "Device 3"),
            onDeviceSelected = {},
            onCancelDevicePayment = {},
            devicePaymentDialog = DevicePaymentDialog.SelectDeviceDialog,
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            onDismiss = {},
            isPaymentSuccessful = false,
            isLoadingInProgress = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicePaymentScreenLoadingDevicesPreview() {
    TTPOADemoAppTheme {
        DevicePaymentScreen(
            connectedDevices = emptyList(),
            onDeviceSelected = {},
            onCancelDevicePayment = {},
            devicePaymentDialog = DevicePaymentDialog.SelectDeviceDialog,
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            onDismiss = {},
            isPaymentSuccessful = false,
            isLoadingInProgress = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    TTPOADemoAppTheme {
        DeviceItem(
            deviceId = "Device 1",
            onDeviceSelected = {},
            isSelected = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicePaymentScreenPaymentInProgressPreview() {
    TTPOADemoAppTheme {
        DevicePaymentScreen(
            connectedDevices = listOf("Device 1", "Device 2", "Device 3"),
            onDeviceSelected = {},
            onCancelDevicePayment = {},
            devicePaymentDialog = DevicePaymentDialog.DevicePaymentInProgressDialog,
            selectedDevice = "V400m-10003234",
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            onDismiss = {},
            isPaymentSuccessful = true,
            isLoadingInProgress = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicePaymentScreenPaymentSuccessPreview() {
    TTPOADemoAppTheme {
        DevicePaymentScreen(
            connectedDevices = listOf("Device 1", "Device 2", "Device 3"),
            onDeviceSelected = {},
            onCancelDevicePayment = {},
            devicePaymentDialog = DevicePaymentDialog.DevicePaymentSuccessDialog,
            selectedDevice = "V400m-10003234",
            onInitiatePaymentOnDevice = {},
            onAbortPaymentOnDevice = {},
            onDismiss = {},
            isPaymentSuccessful = true,
            isLoadingInProgress = false
        )
    }
}