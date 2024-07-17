package com.supertokens.sdk.recipes.emailverification.models

data class VerifyEmailTokenData(
    val userId: String,
    val email: String,
)
