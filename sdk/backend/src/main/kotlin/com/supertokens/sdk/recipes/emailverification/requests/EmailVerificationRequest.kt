package com.supertokens.sdk.recipes.emailverification.requests

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerificationRequest(
    val userId: String,
    val email: String,
)
