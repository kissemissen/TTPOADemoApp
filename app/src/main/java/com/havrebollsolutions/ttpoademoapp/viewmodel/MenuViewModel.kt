package com.havrebollsolutions.ttpoademoapp.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // ktlint-disable import-ordering
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.repository.MenuItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.adyen.ipp.api.InPersonPayments
import com.havrebollsolutions.ttpoademoapp.data.models.Currency
import com.havrebollsolutions.ttpoademoapp.data.repository.UserPreferencesRepository
import com.havrebollsolutions.ttpoademoapp.di.service.AdyenAuthenticationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuItemRepository: MenuItemRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // will improve this part later, does not belong in the viewmodel
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCurrency = MutableStateFlow<Currency>(Currency.SEK)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()


    val cartTotalAmount: StateFlow<Double> = cartItems
        .map { items ->
            items.sumOf { it.menuItem.price * it.quantity }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    init {
        viewModelScope.launch {
            userPreferencesRepository.logotypeUriPath.collect { uriPath ->
                _uiState.value = _uiState.value.copy(logotypeUriPath = uriPath?.toUri())
            }
        }
        viewModelScope.launch {
            Log.d("MenuViewModel", selectedCurrency.value.isoCode)
            menuItemRepository.getAllMenuItems().collect { items ->
//                _menuItems.value = items
                updateMenuItems(items)
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.selectedCurrency.collect { currency ->
                _selectedCurrency.value = currency
            }
        }
    }

    fun updateMenuItems(menuItems: List<MenuItem>) {
        _uiState.update { currentState ->
            currentState.copy(
                menuItems = menuItems
            )
        }
    }

    /**
     * Adds an item to the shopping cart.
     */
    fun addItemToCart(menuItem: MenuItem) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.menuItem.id == menuItem.id }
            if (existingItem != null) {
                currentItems.map {
                    if (it.menuItem.id == menuItem.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
            } else {
                currentItems + CartItem(menuItem, 1)
            }
        }
        Log.d("MenuViewModel", "Cart items: ${cartItems.value}")
    }

    /**
     * Removes an item from the shopping cart.
     */
    fun removeItemFromCart(cartItem: CartItem) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.menuItem.id != cartItem.menuItem.id }
        }
    }

    /**
     * Updates the quantity of an item in the shopping cart.
     */
    fun adjustItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItemFromCart(cartItem)
        } else {
            _cartItems.update { currentItems ->
                currentItems.map {
                    if (it.menuItem.id == cartItem.menuItem.id) {
                        it.copy(quantity = newQuantity)
                    } else {
                        it
                    }
                }
            }
        }
    }


    /**
     * Resets the shopping cart by clearing all items.
     * This should be called after a successful checkout.
     */
    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

/**
 * Ui State for the Menu screen
 */
data class MenuUiState(
    val logotypeUriPath: Uri? = null,
    val selectedCurrency: Currency = Currency.SEK,
    val menuItems: List<MenuItem> = emptyList()
)