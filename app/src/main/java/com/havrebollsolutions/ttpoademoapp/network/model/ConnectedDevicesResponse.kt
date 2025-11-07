package com.havrebollsolutions.ttpoademoapp.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectedDevicesResponse(
    val uniqueDeviceIds: List<String>
)
