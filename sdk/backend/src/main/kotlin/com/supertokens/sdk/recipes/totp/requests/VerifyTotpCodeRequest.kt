package com.supertokens.sdk.recipes.totp.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpCodeRequest(
    val userId: String,
    val totp: String,
    val allowUnverifiedDevices: Boolean = false,
)
