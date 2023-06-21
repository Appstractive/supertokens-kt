package com.supertokens.sdk.recipes.emailpassword.responses

import kotlinx.serialization.Serializable

@Serializable
data class CreateResetPasswordTokenResponse(
    val status: String,
    val token: String,
)