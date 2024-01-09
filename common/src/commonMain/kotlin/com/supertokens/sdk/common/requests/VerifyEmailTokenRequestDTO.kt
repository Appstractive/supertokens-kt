package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailTokenRequestDTO(
    val method: String = "token",
    val token: String,
)
