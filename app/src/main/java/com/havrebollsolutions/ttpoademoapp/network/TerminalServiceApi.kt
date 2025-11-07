package com.havrebollsolutions.ttpoademoapp.network

import com.adyen.ipp.api.authentication.AuthenticationResponse
import com.havrebollsolutions.ttpoademoapp.network.model.AuthenticationCertificateResponse
import com.havrebollsolutions.ttpoademoapp.network.model.ConnectedDevicesResponse
import com.havrebollsolutions.ttpoademoapp.network.model.NexoResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// define an interface, defines how Retrofit talks to the web server using HTTP requests
interface TerminalServiceApi {
    // make suspend to allow calls from coroutines
//    @POST("possdk-sessions")
    // AWS backend example
//    suspend fun getPosSdkSession(@Header("config-id") configId: String,
//                                 @Body body: RequestBody): AuthenticationCertificateResponse
    // Call from frontend, NOT RECOMMENDED in Production App
    @POST("auth/certificate")
    suspend fun getPosSdkSession(
        @Header("x-API-key") apiKey: String,
        @Body body: RequestBody
    ): AuthenticationCertificateResponse

    @GET("merchants/{merchantAccount}/connectedDevices")
    suspend fun getConnectedDevices(
        @Header("x-API-key") apiKey: String,
        @Path("merchantAccount") merchantAccount: String
    ): ConnectedDevicesResponse

    @POST("merchants/{merchantAccount}/devices/{deviceId}/sync")
    suspend fun syncCloudDeviceApiRequest(
        @Header("x-API-key") apiKey: String,
        @Path("merchantAccount") merchantAccount: String,
        @Path("deviceId") deviceId: String,
        @Body body: RequestBody
    ): NexoResponse


    /*
        @POST("nexo")
        suspend fun nexoSyncTerminalPaymentRequest(@Header("config-id") configId: String,
                                                   @Body body: NexoPaymentRequest): NexoPaymentResponse

        @POST("nexo")
        suspend fun nexoSyncTerminalTransactionStatusRequest(@Header("config-id") configId: String,
                                                             @Body body: NexoTransactionStatusRequest): NexoTransactionStatusResponse

        @POST("nexo")
        suspend fun nexoSyncTerminalAbortRequest(@Header("config-id") configId: String,
                                                 @Body body: NexoAbortRequest): String*/
}