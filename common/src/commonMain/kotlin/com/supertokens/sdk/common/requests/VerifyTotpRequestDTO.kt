package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpRequestDTO(
    val totp: String,
)
