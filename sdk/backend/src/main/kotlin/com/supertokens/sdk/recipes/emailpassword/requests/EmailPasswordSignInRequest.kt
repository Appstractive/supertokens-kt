package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class EmailPasswordSignInRequest(
    val email: String,
    val password: String,
)
