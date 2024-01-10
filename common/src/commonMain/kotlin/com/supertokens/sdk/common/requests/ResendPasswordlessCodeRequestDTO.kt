package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use ResendPasswordlessCodeRequestDTO instead")
typealias ResendPasswordlessCodeRequest = ResendPasswordlessCodeRequestDTO

@Serializable
data class ResendPasswordlessCodeRequestDTO(
    val deviceId: String,
    val preAuthSessionId: String,
)
