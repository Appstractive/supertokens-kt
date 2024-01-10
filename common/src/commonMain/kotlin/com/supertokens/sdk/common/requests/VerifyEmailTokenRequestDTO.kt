package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use VerifyEmailTokenRequestDTO instead")
typealias VerifyEmailTokenRequest = VerifyEmailTokenRequestDTO

@Serializable
data class VerifyEmailTokenRequestDTO(
    val method: String = "token",
    val token: String,
)
