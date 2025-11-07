package com.havrebollsolutions.ttpoademoapp.data.repository

import com.havrebollsolutions.ttpoademoapp.network.CloudDeviceApiBaseUrl
import com.havrebollsolutions.ttpoademoapp.network.SoftposConfigBaseUrl
import com.havrebollsolutions.ttpoademoapp.network.TerminalServiceApi
import com.havrebollsolutions.ttpoademoapp.network.model.AuthenticationCertificateResponse
import com.havrebollsolutions.ttpoademoapp.network.model.NexoResponse
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * Repository to interact with the terminal service api.
 */
interface TerminalRepository {
    /**
     * Fetches Adyen TTP SDK authentication certificate
     */
    suspend fun getPosSdkSession(
        apiKey: String,
        body: RequestBody
    ): AuthenticationCertificateResponse

    suspend fun getConnectedDevices(
        apiKey: String,
        merchantAccount: String,
        store: String
    ): List<String>

    suspend fun syncCloudDeviceApiRequest(
        apiKey: String,
        merchantAccount: String,
        deviceId: String,
        body: RequestBody
    ): NexoResponse
}

class NetworkTerminalRepository @Inject constructor(
    //    private val terminalServiceApi: TerminalServiceApi,
    // 1. Inject the instance meant for making the pos sdk session calls
    @SoftposConfigBaseUrl private val softposConfigTerminalServiceApi: TerminalServiceApi,
    // 2. Inject the instance meant for making the Cloud Device Api calls
    @CloudDeviceApiBaseUrl private val cloudDeviceApiTerminalServiceApi: TerminalServiceApi

) : TerminalRepository {
    override suspend fun getPosSdkSession(
//        configId: String,
        apiKey: String,
        body: RequestBody
    ): AuthenticationCertificateResponse {
//        return terminalServiceApi.getPosSdkSession(configId, body)
        return softposConfigTerminalServiceApi.getPosSdkSession(apiKey, body)
    }

    override suspend fun getConnectedDevices(
        apiKey: String,
        merchantAccount: String,
        store: String
    ): List<String> {
        return cloudDeviceApiTerminalServiceApi.getConnectedDevices(
            apiKey=apiKey,
            merchantAccount=merchantAccount
        ).uniqueDeviceIds
    }

    override suspend fun syncCloudDeviceApiRequest(
        apiKey: String,
        merchantAccount: String,
        deviceId: String,
        body: RequestBody
    ): NexoResponse {
        return cloudDeviceApiTerminalServiceApi.syncCloudDeviceApiRequest(
            apiKey=apiKey,
            merchantAccount=merchantAccount,
            deviceId = deviceId,
            body = body
        )
    }
}