package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class EmailPasswordSignUpRequest(
    val email: String,
    val password: String,
)
