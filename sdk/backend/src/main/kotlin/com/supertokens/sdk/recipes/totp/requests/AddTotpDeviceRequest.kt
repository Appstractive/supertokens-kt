package com.supertokens.sdk.recipes.totp.requests

import kotlinx.serialization.Serializable

@Serializable
data class AddTotpDeviceRequest(
    val userId: String,
    val deviceName: String,
    val skew: Int,
    val period: Int,
    val secretKey: String? = null,
)
