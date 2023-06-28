package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RegenerateSessionRequest(
    val accessToken: String,
    val userDataInJWT: JsonElement? = null,
)
