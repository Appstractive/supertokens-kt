package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateResetPasswordTokenRequest(
    val userId: String,
)