package com.supertokens.sdk.recipes.passwordless.requests

import kotlinx.serialization.Serializable

@Serializable
data class RevokeAllPasswordlessCodesRequest(
    val email: String? = null,
    val phoneNumber: String? = null,
)
