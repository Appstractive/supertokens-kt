package com.supertokens.sdk.recipes.roles.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteRoleRequest(
    val role: String,
)
