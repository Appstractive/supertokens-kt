package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UpdateJwtDataRequest(
    val sessionHandle: String,
    val userDataInJWT: JsonElement?,
)
