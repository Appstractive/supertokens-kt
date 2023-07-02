package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailTokenRequest(
    val method: String = "token",
    val token: String,
)
