package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable

@Serializable
data class RemoveSessionsRequest(
    val sessionHandles: List<String>,
)
