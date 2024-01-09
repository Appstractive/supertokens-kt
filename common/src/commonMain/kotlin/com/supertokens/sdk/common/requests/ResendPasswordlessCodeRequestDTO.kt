package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResendPasswordlessCodeRequestDTO(
    val deviceId: String,
    val preAuthSessionId: String,
)
