package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithTokenRequest(
    val method: String = "token",
    val token: String,
    val newPassword: String,
)