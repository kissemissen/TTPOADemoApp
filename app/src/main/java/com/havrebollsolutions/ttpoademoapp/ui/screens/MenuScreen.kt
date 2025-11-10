package com.havrebollsolutions.ttpoademoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.havrebollsolutions.ttpoademoapp.viewmodel.MenuViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.havrebollsolutions.ttpoademoapp.R
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import com.havrebollsolutions.ttpoademoapp.ui.components.AppBar
import com.havrebollsolutions.ttpoademoapp.ui.components.MenuItemCard
import com.havrebollsolutions.ttpoademoapp.ui.theme.TTPOADemoAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isTabletLayout: Boolean = false,
    canNavigateBack: Boolean = false,
    viewModel: MenuViewModel = hiltViewModel()
) {
    // 1. Access UI State of the view model
    val menuUiState by viewModel.uiState.collectAsState()

    // 2. Collect the cart items state from the ViewModel.
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            if (!isTabletLayout) {
                AppBar(
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = onNavigateBack,
                    onNavigateToSettings = onNavigateToSettings,
                    logoUri = menuUiState.logotypeUriPath
                )
            }
        },
        floatingActionButton = {
            if (!isTabletLayout) {
                CartFab(cartItemCount, onNavigateToCart)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuItemList(
                selectedCurrency = selectedCurrency,
                menuItems = menuUiState.menuItems,
                onAddItemToCart = viewModel::addItemToCart,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.made_with_3_by_j_kisselgof_2025),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
fun MenuItemList(
    selectedCurrency: Currency,
    menuItems: List<MenuItem>,
    onAddItemToCart: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = paddingValues,
        modifier = modifier
            .padding(16.dp)
    ) {
        items(menuItems) { item ->
            MenuItemCard(
                selectedCurrency = selectedCurrency,
                menuItem = item,
                onAddItemToCart = { onAddItemToCart(item) })
        }
    }
}

@Composable
fun CartFab(
    cartItemCount: Int,
    onNavigateToCart: () -> Unit
) {
    // 2. Wrap the FAB and the badge in a Box.
    if (cartItemCount > 0) {
        Box(contentAlignment = Alignment.TopStart) {
            FloatingActionButton(onClick = onNavigateToCart) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
            }
            // 3. Conditionally display the badge if there are items.
            Text(
                text = cartItemCount.toString(),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(x = (-8).dp, y = (-8).dp) // Offset to position the badge.
                    .size(24.dp)
                    .background(Color.Red, shape = CircleShape) // Create a red circle.
                    .padding(0.dp)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MenuItemListPreview() {
    val mockMenuItem0 = MenuItem(
        id = 1,
        name = "Test Item",
        description = "Test Description",
        price = 10.0,
        vatRate = 10.0,
        quantityInStock = 10,
        imagePath = null
    )
    val mockMenuItem1 = MenuItem(
        id = 2,
        name = "Test Item",
        description = "Test Description",
        price = 10.0,
        vatRate = 10.0,
        quantityInStock = 10,
        imagePath = null
    )
    val mockMenuItem2 = MenuItem(
        id = 3,
        name = "Test Item",
        description = "Test Description",
        price = 20.0,
        vatRate = 10.0,
        quantityInStock = 10,
        imagePath = null
    )
    TTPOADemoAppTheme {
        MenuItemList(
            menuItems = listOf(mockMenuItem0, mockMenuItem1, mockMenuItem2),
            onAddItemToCart = {},
            selectedCurrency = Currency.SEK
        )
    }
}

@Composable
@Preview(showBackground = true)
fun CartFabEnabledPreview() {
    CartFab(cartItemCount = 5, onNavigateToCart = {})
}

@Composable
@Preview(showBackground = true)
fun CartFabDisbledPreview() {
    CartFab(cartItemCount = 0, onNavigateToCart = {})
}