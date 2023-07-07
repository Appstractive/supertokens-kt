package com.supertokens.sdk.recipes.totp.requests

import kotlinx.serialization.Serializable

@Serializable
data class RemoveTotpDeviceRequest(
    val userId: String,
    val deviceName: String,
)
