package com.havrebollsolutions.ttpoademoapp.data.repository

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.net.http.HttpException
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresExtension
import com.adyen.ipp.api.InPersonPayments
import com.adyen.ipp.api.authentication.AuthenticationResponse
import com.adyen.ipp.api.payment.PaymentInterfaceType
import com.adyen.ipp.api.payment.TransactionRequest
import com.adyen.ipp.api.ui.MerchantUiParameters
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.havrebollsolutions.ttpoademoapp.network.model.NexoResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PaymentRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val terminalRepository: TerminalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun makeSdkPayment(
        launcher: ActivityResultLauncher<Intent>,
        currency: String = "SEK",
        amount: Double = 10.0,
        paymentInterfaceType: PaymentInterfaceType = PaymentInterfaceType.Companion.createTapToPayType()
    ) {
        val paymentInterfaceType = paymentInterfaceType
        val paymentInterface =
            InPersonPayments.getPaymentInterface(paymentInterfaceType)
        Log.d("PaymentRepository", InPersonPayments.getInstallationId().getOrThrow())

        InPersonPayments.performTransaction(
            context = context,
            paymentInterface = paymentInterface.getOrThrow(),
            transactionRequest = TransactionRequest.Companion.create(
                generateNexoRequest(
                    requestedAmount = "$amount",
                    currency = currency,
                    poiId = InPersonPayments.getInstallationId().getOrThrow()
                )
            ).getOrThrow(),
            paymentLauncher = launcher,
//            authenticationServiceClass = MyAuthenticationService::class.java,
            merchantUiParameters = MerchantUiParameters.Companion.create(
                autoDismissDelay = 1000.milliseconds,
                tapToPayUiParameters = MerchantUiParameters.TapToPayUiParameters.create(
                    MerchantUiParameters.TapToPayUiParameters.TapToPayAnimationType.default()
                ),
                kioskModeUiParameters = MerchantUiParameters.KioskModeUiParameters.create(
                    applyToAllTablets = true,
                    pinInputAlignment = MerchantUiParameters.KioskModeUiParameters.PinInputAlignment.Right,
                    tapToPayKioskAnimation = MerchantUiParameters.KioskModeUiParameters.TapToPayKioskAnimationType.front(
                        MerchantUiParameters.KioskModeUiParameters.TapToPayKioskAnimationType.Front.NfcFrontPosition.Center
                    )
                )
            )
        )
    }

    /**
     * Gets a list of connected devices.
     * @return A list of connected devices.
     */
    suspend fun getConnectedDevices(): List<String> {
        val connectedDevices = terminalRepository.getConnectedDevices(
            apiKey = userPreferencesRepository.adyenConfig.first()!!.apiKey,
            merchantAccount = userPreferencesRepository.adyenConfig.first()!!.merchantAccount,
            store = userPreferencesRepository.adyenConfig.first()!!.store
        )
        Log.d("PaymentRepository", "Connected Devices: $connectedDevices")
        return connectedDevices
    }

    /**
     * Makes a device payment.
     *
     */
    suspend fun makeDevicePayment(
        currency: String = "SEK",
        amount: Double = 10.0,
        poiId: String,
        serviceId: String = "SID" + (100000..999999).random().toString()
    ) : NexoResponse {
//        val connectedDevices = terminalRepository.getConnectedDevices(
//            apiKey = userPreferencesRepository.adyenConfig.first()!!.apiKey,
//            merchantAccount = userPreferencesRepository.adyenConfig.first()!!.merchantAccount,
//            store = userPreferencesRepository.adyenConfig.first()!!.store
//        )
//        Log.d("PaymentRepository", "Connected Devices: $connectedDevices")

        // Generate nexo request
        val nexoPaymentRequest = generateNexoRequest(
            requestedAmount = "$amount",
            currency = currency,
            poiId = poiId,
            serviceId = serviceId
        )

        // define media type
        val mediaType = "application/json".toMediaType()

        // Create request body
        val requestBody = nexoPaymentRequest.toRequestBody(mediaType)

        // Call terminal repository to initiate device payment
        return terminalRepository.syncCloudDeviceApiRequest(
            apiKey = userPreferencesRepository.adyenConfig.first()!!.apiKey,
            merchantAccount = userPreferencesRepository.adyenConfig.first()!!.merchantAccount,
            deviceId = poiId,
            body = requestBody
        )
    }

    suspend fun abortDevicePayment(
        poiId: String,
        originalPaymentServiceId: String
    ) {
        val nexoAbortRequest = generateNexoAbortRequest(
            originalPaymentServiceId = originalPaymentServiceId,
            poiId = poiId
        )

        // define media type
        val mediaType = "application/json".toMediaType()

        // Create request body
        val requestBody = nexoAbortRequest.toRequestBody(mediaType)

        // Call terminal repository to initiate device payment
        terminalRepository.syncCloudDeviceApiRequest(
            apiKey = userPreferencesRepository.adyenConfig.first()!!.apiKey,
            merchantAccount = userPreferencesRepository.adyenConfig.first()!!.merchantAccount,
            deviceId = poiId,
            body = requestBody
        )
    }

    /**
     * Decodes a Base64-encoded JSON string and attempts to parse it into a NexoResponse object using Gson.
     *
     * Steps:
     * 1. Decodes the input Base64 string to a byte array. Logs and returns null on invalid Base64.
     * 2. Converts the byte array to a plain JSON string (UTF-8).
     * 3. Uses Gson to deserialize the JSON string into a NexoResponse object.
     * 4. Logs and returns null if the JSON is malformed or the structure doesn't match NexoResponse.
     *
     * @param base64response The Base64-encoded string containing the JSON data.
     * @return The parsed NexoResponse object, or null if decoding or parsing fails.
     */
    fun parseNexoResponseFromBase64(encodedNexoResponse: String): NexoResponse? {
        // Decode Base64 String
        val decodedBytes: ByteArray = try {
            Base64
                .decode(encodedNexoResponse, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Log.e("PaymentTtp", "Error: Invalid Base64 string format.")
            Log.e("PaymentTtp", e.printStackTrace().toString())
            e.printStackTrace()
            return null
        }

        // Convert into String
        val decodedString: String = String(decodedBytes, StandardCharsets.UTF_8)
        Log.d("OverviewScreen", decodedString)

        // Serialize and return
        return try {
            Gson().fromJson(decodedString, NexoResponse::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("PaymentTtp", "Error: Failed to parse JSON. Structure mismatch or invalid JSON.")
            Log.e("PaymentTtp", e.printStackTrace().toString())
            null
        }
    }

    companion object {

        private val DATE_FORMAT =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

        /**
         * Generates a Nexo request.
         *
         */
        fun generateNexoRequest(
            serviceId: String = "SID" + (100000..999999).random().toString(),
            saleId: String = "TTPOADemoApp",
            transactionID: String = UUID.randomUUID().toString(),
            poiId: String,
            currency: String,
            requestedAmount: String,
        ): String {

            val timeStamp = DATE_FORMAT.format(Date())
            return "{\n" +
                    "  \"SaleToPOIRequest\": {\n" +
                    "    \"MessageHeader\": {\n" +
                    "      \"ProtocolVersion\": \"3.0\",\n" +
                    "      \"MessageClass\": \"Service\",\n" +
                    "      \"MessageCategory\": \"Payment\",\n" +
                    "      \"MessageType\": \"Request\",\n" +
                    "      \"ServiceID\": \"$serviceId\",\n" +
                    "      \"SaleID\": \"$saleId\",\n" +
                    "      \"POIID\": \"$poiId\"\n" +
                    "    },\n" +
                    "    \"PaymentRequest\": {\n" +
                    "      \"SaleData\": {\n" +
//                    "        \"SaleToAcquirerData\": \"split.api=1&split.nrOfItems=2&split.totalAmount=10000&split.currencyCode=SEK&split.item1.amount=8000&split.item1.type=BalanceAccount&split.item1.account=BA3222Z22322675JT5M72CHCX&split.item1.reference=reference_split_1&split.item1.description=description_split_1&split.item2.amount=2000&split.item2.type=Commission&split.item2.reference=reference_commission&split.item2.description=description_commission\",\n" +
                    "        \"SaleToAcquirerData\": \"applicationInfo.merchantApplication.name=TTPOADemoApp\",\n" +
//                    "        \"SaleToAcquirerData\": \"ewogICAgImFwcGxpY2F0aW9uSW5mbyI6IHsKICAgICAgICAibWVyY2hhbnRBcHBsaWNhdGlvbiI6IHsKICAgICAgICAgICAgIm5hbWUiOiAiTkFNRV9PRl9QT1NfQVBQTElDQVRJT04iLAogICAgICAgICAgICAidmVyc2lvbiI6ICIwLjEwMC4xIgogICAgICAgIH0KICAgIH0KfQ==\",\n" +
                    "        \"SaleTransactionID\": {\n" +
                    "          \"TransactionID\": \"$transactionID\",\n" +
                    "          \"TimeStamp\": \"$timeStamp\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"PaymentTransaction\": {\n" +
                    "        \"AmountsReq\": {\n" +
                    "          \"Currency\": \"$currency\",\n" +
                    "          \"RequestedAmount\": $requestedAmount\n" +
//                    "          \"RequestedAmount\": 100.00" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"

        }

        /**
         * Generates a Nexo abort request.
         *
         */
        fun generateNexoAbortRequest(
            originalPaymentServiceId: String,
            poiId: String,
            saleId: String = "TTPOADemoApp",
            serviceId: String = "SID" + (100000..999999).random().toString()
        ): String {
            return "{\n" +
                    "  \"SaleToPOIRequest\": {\n" +
                    "    \"MessageHeader\": {\n" +
                    "      \"ProtocolVersion\": \"3.0\",\n" +
                    "      \"MessageClass\": \"Service\",\n" +
                    "      \"MessageCategory\": \"Abort\",\n" +
                    "      \"MessageType\": \"Request\",\n" +
                    "      \"ServiceID\": \"$serviceId\",\n" +
                    "      \"SaleID\": \"$saleId\",\n" +
                    "      \"POIID\": \"$poiId\"\n" +
                    "    },\n" +
                    "    \"AbortRequest\": {\n" +
                    "      \"AbortReason\": \"MerchantAbort\",\n" +
                    "      \"MessageReference\": {\n" +
                    "          \"MessageCategory\": \"Payment\",\n" +
                    "          \"SaleID\": \"$saleId\",\n" +
                    "          \"ServiceID\": \"$originalPaymentServiceId\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"
        }
    }
}