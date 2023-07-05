package com.supertokens.sdk.recipes.roles.requests

import kotlinx.serialization.Serializable

@Serializable
data class RemoveRolePermissionsRequest(
    val role: String,
    val permissions: List<String>,
)
