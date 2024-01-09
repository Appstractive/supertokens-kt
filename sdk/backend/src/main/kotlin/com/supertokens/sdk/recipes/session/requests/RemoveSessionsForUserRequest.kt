package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable

@Serializable
data class RemoveSessionsForUserRequest(
    val userId: String,
    val revokeAcrossAllTenants: Boolean? = null,
)
