package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpDeviceRequestDTO(
    val deviceName: String,
    val totp: String,
)
