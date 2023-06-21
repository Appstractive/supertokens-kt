package com.supertokens.sdk.recipes.emailpassword.responses

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithTokenResponse(
    val status: String,
    val userId: String,
)