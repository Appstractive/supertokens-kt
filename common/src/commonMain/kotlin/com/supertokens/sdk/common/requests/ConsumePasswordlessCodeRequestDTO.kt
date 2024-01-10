package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use ConsumePasswordlessCodeRequestDTO instead")
typealias ConsumePasswordlessCodeRequest = ConsumePasswordlessCodeRequestDTO

@Serializable
data class ConsumePasswordlessCodeRequestDTO(
    val preAuthSessionId: String,
    val linkCode: String? = null,
    val deviceId: String? = null,
    val userInputCode: String? = null,
)
