package com.havrebollsolutions.ttpoademoapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havrebollsolutions.ttpoademoapp.data.repository.PaymentRepository
import com.havrebollsolutions.ttpoademoapp.data.repository.UserPreferencesRepository
import com.havrebollsolutions.ttpoademoapp.network.model.NexoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject

sealed class DevicePaymentDialog(
    val route: String
) {
    object SelectDeviceDialog : DevicePaymentDialog("selectDeviceDialog")
    object DevicePaymentInProgressDialog : DevicePaymentDialog("devicePaymentInProgressDialog")
    object DevicePaymentSuccessDialog : DevicePaymentDialog("devicePaymentSuccessDialog")
}

@HiltViewModel
class DevicePaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DevicePaymentUiState())
    val uiState: StateFlow<DevicePaymentUiState> = _uiState

    fun updateConnectedDevices() {
        viewModelScope.launch {
            updateLoadingInProgress()
            try {
                // fetch devices
                val devices = paymentRepository.getConnectedDevices()
                // update ui state
                _uiState.update { currentState ->
                    currentState.copy(
                        connectedDevices = devices
                    )
                }
            } catch (e: JSONException) {
                Log.e("PaymentRepository", "response: JSON PARSING ERROR", e)
            } catch (e: IOException) {
                Log.e("PaymentRepository", "response: connection error", e)
            } catch (e: Exception) {
                Log.e("PaymentRepository", "response: ERROR", e)
            }
            updateLoadingInProgress()
        }
    }

    /**
     * Updates the selected device.
     * @param deviceId The ID of the selected device.
     */
    fun updateSelectedDevice(deviceId: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedDevice = deviceId
            )
        }
    }

    fun initatePaymentOnDevice(amount: Double) {
        updateDevicePaymentDialog(DevicePaymentDialog.DevicePaymentInProgressDialog)
        updateServiceId(serviceId = "SID" + (100000..999999).random().toString())
        viewModelScope.launch {
            try {
                val nexoResponse = paymentRepository.makeDevicePayment(
                    currency = userPreferencesRepository.selectedCurrency.first().isoCode,
                    poiId = _uiState.value.selectedDevice!!,
                    amount = amount,
                    serviceId = _uiState.value.serviceId!!
                )
                updateNexoResponse(nexoResponse)
                // check if payment was successful
                Log.e("PaymentRepository", "response: ${nexoResponse.saleToPOIResponse.paymentResponse.response.result}")
                if (nexoResponse.saleToPOIResponse.paymentResponse.response.result == "Success") {
                    updateIsPaymentSuccessful(true)
                }
            } catch (e: JSONException) {
                Log.d("AdyenAuthenticationProvider", "response: JSON ERROR", e)
            } catch (e: Exception) {
                Log.d("AdyenAuthenticationProvider", "response: ERROR", e)
            }
            updateDevicePaymentDialog(DevicePaymentDialog.DevicePaymentSuccessDialog)
        }
    }

    fun abortPaymentOnDevice() {
        viewModelScope.launch {
            try {
                paymentRepository.abortDevicePayment(
                    poiId = _uiState.value.selectedDevice!!,
                    originalPaymentServiceId = _uiState.value.serviceId!!
                )
            } catch (e: JSONException) {
                Log.d("AdyenAuthenticationProvider", "response: JSON ERROR", e)
            } catch (e: Exception) {
                Log.d("AdyenAuthenticationProvider", "response: ERROR", e)
            }
        }
    }


    /**
     * Updates the device payment dialog.
     * @param devicePaymentDialog The new device payment dialog.
     */
    fun updateDevicePaymentDialog(devicePaymentDialog: DevicePaymentDialog) {
        _uiState.update { currentState ->
            currentState.copy(
                devicePaymentDialog = devicePaymentDialog
            )
        }
    }

    /**
     * Updates the Nexo response.
     * @param nexoResponse The new Nexo response.
     */
    fun updateNexoResponse(nexoResponse: NexoResponse?) {
        _uiState.update { currentState ->
            currentState.copy(
                nexoResponse = nexoResponse
            )
        }
    }

    /**
     * Cancels the device payment by resetting the UI state.
     */
    fun cancelDevicePyment() {
        updateSelectedDevice(null)
        _uiState.update { currentState ->
            currentState.copy(
                connectedDevices = emptyList()
            )
        }
        updateServiceId(null)
        updateIsPaymentSuccessful(false)
    }

    /**
     * Updates the service ID.
     * @param serviceId The new service ID.
     */
    private fun updateServiceId(serviceId: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                serviceId = serviceId
            )
        }
    }

    /**
     * Update payment success state
     */
    private fun updateIsPaymentSuccessful(isPaymentSuccessful: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isPaymentSuccessful = isPaymentSuccessful
            )
        }
    }

    /**
     * Updates the loadingInProgress state.
     */
    private fun updateLoadingInProgress() {
        _uiState.update { currentState ->
            currentState.copy(
                loadingInProgress = !currentState.loadingInProgress
            )
        }
    }
}

data class DevicePaymentUiState(
    val devicePaymentDialog: DevicePaymentDialog = DevicePaymentDialog.SelectDeviceDialog,
    val selectedDevice: String? = null,
    val loadingInProgress: Boolean = false,
    val connectedDevices: List<String> = emptyList(),
    val isPaymentSuccessful: Boolean = false,
    val serviceId: String? = null,
    val nexoResponse: NexoResponse? = null
)