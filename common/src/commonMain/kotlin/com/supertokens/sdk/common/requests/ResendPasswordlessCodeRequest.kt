package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResendPasswordlessCodeRequest(
    val deviceId: String,
    val preAuthSessionId: String,
)
