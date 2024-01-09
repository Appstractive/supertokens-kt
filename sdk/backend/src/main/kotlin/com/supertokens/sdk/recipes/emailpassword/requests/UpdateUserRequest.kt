package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val recipeUserId: String,
    val email: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null,
)
