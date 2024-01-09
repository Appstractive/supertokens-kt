package com.supertokens.sdk.recipes.passwordless.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreatePasswordlessCodeRequest(
    val email: String? = null,
    val phoneNumber: String? = null,
    val deviceId: String? = null,
    val userInputCode: String? = null,
)
