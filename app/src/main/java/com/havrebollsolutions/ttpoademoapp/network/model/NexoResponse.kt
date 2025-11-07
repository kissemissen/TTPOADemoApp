package com.havrebollsolutions.ttpoademoapp.network.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    @SerializedName("Result")
    val result: String
)

@Serializable
data class POITransactionID(
    @SerializedName("TransactionID")
    val transactionID: String? = null
)

@Serializable
data class POIData(
    @SerializedName("POITransactionID")
    val poiTransactionID: POITransactionID? = null
)

@Serializable
data class PaymentResponse(
    @SerializedName("POIData")
    val poidData: POIData? = null,
    @SerializedName("Response")
    val response: Response
)


@Serializable
data class SaleToPOIResponse(
    @SerializedName("PaymentResponse")
    val paymentResponse: PaymentResponse
)

@Serializable
data class NexoResponse(
    @SerializedName("SaleToPOIResponse")
    val saleToPOIResponse: SaleToPOIResponse
)