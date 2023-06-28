package com.supertokens.sdk.recipes.session.responses

import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsResponse(
    val status: String,
    val sessionHandles: List<String>,
)
