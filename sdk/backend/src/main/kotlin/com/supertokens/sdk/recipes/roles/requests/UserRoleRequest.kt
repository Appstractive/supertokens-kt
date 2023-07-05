package com.supertokens.sdk.recipes.roles.requests

import kotlinx.serialization.Serializable

@Serializable
data class UserRoleRequest(
    val userId: String,
    val role: String,
)
