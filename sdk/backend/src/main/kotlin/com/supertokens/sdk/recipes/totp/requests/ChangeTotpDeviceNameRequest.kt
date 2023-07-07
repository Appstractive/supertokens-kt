package com.supertokens.sdk.recipes.totp.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeTotpDeviceNameRequest(
    val userId: String,
    val existingDeviceName: String,
    val newDeviceName: String,
)
