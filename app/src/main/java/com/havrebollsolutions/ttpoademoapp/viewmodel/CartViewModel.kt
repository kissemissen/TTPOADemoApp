package com.havrebollsolutions.ttpoademoapp.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.ipp.api.payment.PaymentInterfaceType
import com.havrebollsolutions.ttpoademoapp.data.repository.PaymentRepository
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Order
import com.havrebollsolutions.ttpoademoapp.data.repository.OrderRepository
import com.havrebollsolutions.ttpoademoapp.data.repository.UserPreferencesRepository
import com.havrebollsolutions.ttpoademoapp.network.model.NexoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject


sealed class PaymentOption(val name: String) {
    object TapToPay : PaymentOption("Tap to Pay")
    object NYC1 : PaymentOption("NYC1")
    object Terminal : PaymentOption("Terminal")
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    // 💡 Inject the PaymentTtp class with only the ApplicationContext (safe for Hilt)
//    private val paymentTtp: PaymentTtp = PaymentTtp(applicationContext)

    init {
        viewModelScope.launch {
            userPreferencesRepository.logotypeUriPath.collect { uriPath ->
                _uiState.value = _uiState.value.copy(logotypeUriPath = uriPath?.toUri())
            }
        }
    }

    fun decodeBase64NexoResponse(encodedNexoResponse: String): NexoResponse? {
        return paymentRepository.parseNexoResponseFromBase64(encodedNexoResponse)
    }

    fun checkout(
        nexoResponse: NexoResponse?,
        cartItems: List<CartItem>,
        paymentMethod: String,
        clearCart: () -> Unit
    ) {
        if (nexoResponse?.saleToPOIResponse?.paymentResponse?.response?.result == "Success") {
            viewModelScope.launch {
                val totalAmount = cartItems.sumOf { it.menuItem.price * it.quantity }

                // 1. Create the Order object
                val order = Order(
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    transactionId = nexoResponse
                        .saleToPOIResponse
                        .paymentResponse
                        .poidData!!
                        .poiTransactionID!!
                        .transactionID!!
                )

                // 2. Pass the Order and its items to the repository for a single, complete operation
                orderRepository.saveOrder(order, cartItems)

                //Clear cart
                clearCart()

                // Allow new payments
                updatePaymentInProgress(false)
                updateIsCartPaid(true)
                updateSelectedPaymentOption(null)
            }
        } else {
            // Handle the failure scenario
            updatePaymentInProgress(false)
        }
    }

    /**
     * Initiates a payment process using the Adyen Tap to Pay SDK.
     * Used for both Tap to Pay and Card Reader Payments.
     *
     * @param amount The amount to be paid. Defaults to 10.0.
     * @param launcher An [ActivityResultLauncher] to handle the result of the payment activity.
     *                 This is typically provided by the calling Composable or Fragment.
     */
    fun initiateSdkPayment(amount: Double = 10.0, launcher: ActivityResultLauncher<Intent>) {
        val paymentInterfaceType = if (uiState.value.selectedPaymentOption == PaymentOption.TapToPay) {
            PaymentInterfaceType.Companion.createTapToPayType()
        } else {
            PaymentInterfaceType.Companion.createCardReaderType()
        }
        viewModelScope.launch {
//            val paymentTtp = PaymentTtp(context, launcher)
            paymentRepository.makeSdkPayment(
                amount = amount,
                launcher = launcher,
                currency = userPreferencesRepository.selectedCurrency.first().isoCode,
                paymentInterfaceType = paymentInterfaceType
            )
        }
    }

    /**
     * Initiates a device payment process.
     * @param amount The amount to be paid. Defaults to 10.0.
     */
    fun initiateDevicePayment(
        amount: Double = 10.0
    ) {
//        viewModelScope.launch {
//            try {
//                paymentRepository.makeDevicePayment(
//                    amount = amount,
//                    currency = userPreferencesRepository.selectedCurrency.first().isoCode
//                )
//            } catch (e: JSONException) {
//                Log.e("PaymentRepository", "response: JSON PARSING ERROR", e)
//            } catch (e: IOException) {
//                Log.e("PaymentRepository", "response: connection error", e)
//            } catch (e: Exception) {
//                Log.e("PaymentRepository", "response: ERROR", e)
//            }
//
//            updatePaymentInProgress(false)
//        }
    }

    /**
     * Initiates a payment process based on the selected payment option.
     * @param amount The amount to be paid. Defaults to 10.0.
     * @param launcher An [ActivityResultLauncher] to handle the result of the payment activity.
     *                 This is typically provided by the calling Composable or Fragment.
     */
    fun initiatePayment(
        amount: Double = 10.0,
        launcher: ActivityResultLauncher<Intent>
    ) {
        if (uiState.value.selectedPaymentOption == PaymentOption.Terminal) {
            initiateDevicePayment()
        } else {
            initiateSdkPayment(amount, launcher)
        }
    }

    fun updateSelectedPaymentOption(paymentOption: PaymentOption?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedPaymentOption = paymentOption
            )
        }
    }

    fun updatePaymentInProgress(inProgress: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                paymentInProgess = inProgress
            )
        }
    }

    fun updateIsCartPaid(isPaid: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isCartPaid = isPaid
            )
        }
    }
}

data class CartUiState(
    val logotypeUriPath: Uri? = null,
    val selectedPaymentOption: PaymentOption? = null,
    val paymentInProgess: Boolean = false,
    val isCartPaid: Boolean = false
)
