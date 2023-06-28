package com.supertokens.sdk.recipes.session.responses

import kotlinx.serialization.Serializable

@Serializable
data class RemoveSessionsResponse(
    val status: String,
    val sessionHandlesRevoked: List<String>,
)
