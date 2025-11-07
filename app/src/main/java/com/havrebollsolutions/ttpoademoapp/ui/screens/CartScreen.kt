package com.havrebollsolutions.ttpoademoapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.havrebollsolutions.ttpoademoapp.ui.components.AppBar
import com.havrebollsolutions.ttpoademoapp.ui.components.CartItemRow
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel

@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOverview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isTabletLayout: Boolean = false,
    canNavigateBack: Boolean = false,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    Scaffold(
        topBar =  {
            if (!isTabletLayout) {
                AppBar(
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = onNavigateBack,
                    onNavigateToSettings = onNavigateToSettings,
                    logoUri = viewModel.uiState.collectAsState().value.logotypeUriPath
                )
            }
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (cartItems.isEmpty()) {
                    Text(
                        text = "Your cart is empty!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(cartItems) { cartItem ->
                            CartItemRow(
                                selectedCurrency = selectedCurrency,
                                cartItem = cartItem,
                                onQuantityChanged = { newQuantity ->
                                    viewModel.adjustItemQuantity(cartItem, newQuantity)
                                },
                                onRemoveItem = { viewModel.removeItemFromCart(cartItem) }
                            )
                            HorizontalDivider(thickness = 1.dp) // Use this for a clear divider
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isTabletLayout) {
                    Button(
                        onClick = onNavigateToOverview,
                        enabled = cartItems.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Proceed to Overview")
                    }
                }
            }
        }
    }
}