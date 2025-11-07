package com.havrebollsolutions.ttpoademoapp.data.repository

import android.util.Log
import com.adyen.ipp.api.authentication.AuthenticationProvider
import com.adyen.ipp.api.authentication.AuthenticationResponse
import com.havrebollsolutions.ttpoademoapp.BuildConfig
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class AdyenAuthenticationProvider @Inject constructor(
    private val terminalRepository: TerminalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthenticationProvider {
    override suspend fun authenticate(setupToken: String): Result<AuthenticationResponse> {
        // Build request json
        val jsonObject = JSONObject().apply {
            put("merchantAccount", userPreferencesRepository.adyenConfig.first()!!.merchantAccount)
            if (userPreferencesRepository.adyenConfig.first()!!.store.isNotBlank()) {
                put("store", userPreferencesRepository.adyenConfig.first()!!.store)
            }
            put("setupToken", setupToken)
        }

        // define media type
        val mediaType = "application/json".toMediaType()

        // Create request body
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

//        val configId = BuildConfig.TERMINAL_SERVICE_CONFIG_ID
        return try {
//            Log.d("AdyenAuthenticationProvider", "DB 3: $configId")
            val authenticationCertificateResponse =
//                terminalRepository.getPosSdkSession(configId, requestBody)
                terminalRepository.getPosSdkSession(
                    apiKey = userPreferencesRepository.adyenConfig.first()!!.apiKey,
                    body = requestBody
                )
            Log.d(
                "AdyenAuthenticationProvider",
                "terminalRepositoryResponse: $authenticationCertificateResponse"
            )
            Result.success(AuthenticationResponse.Companion.create(authenticationCertificateResponse.sdkData))
        } catch (e: JSONException) {
            Log.d("AdyenAuthenticationProvider", "response: ERROR")
            Result.failure(Throwable("error"))
        } catch (e: Exception) {
            Log.d("AdyenAuthenticationProvider", "response: ERROR")
            Result.failure(Throwable(e))
        }
    }
}