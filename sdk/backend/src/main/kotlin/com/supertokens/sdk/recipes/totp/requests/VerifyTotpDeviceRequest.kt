package com.supertokens.sdk.recipes.totp.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpDeviceRequest(
    val userId: String,
    val deviceName: String,
    val totp: String,
)
