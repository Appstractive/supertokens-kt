package com.supertokens.sdk.recipes.passwordless.requests

import kotlinx.serialization.Serializable

@Serializable
data class ConsumePasswordlessCodeRequest(
    val preAuthSessionId: String,
    val linkCode: String? = null,
    val deviceId: String? = null,
    val userInputCode: String? = null,
)
