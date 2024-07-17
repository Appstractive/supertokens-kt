package com.supertokens.sdk.recipes.session.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SessionResponse(
    val handle: String,
    val userId: String,
    val userDataInJWT: JsonObject? = null,
)
