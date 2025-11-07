package com.havrebollsolutions.ttpoademoapp.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationCertificateResponse(
    val id: String,
    val merchantAccount: String,
    val store: String? = null,
    val installationId: String,
    val sdkData: String
)