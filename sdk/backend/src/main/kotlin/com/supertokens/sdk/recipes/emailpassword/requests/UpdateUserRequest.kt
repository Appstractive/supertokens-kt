package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val userId: String,
    val email: String? = null,
    val password: String? = null,
)
