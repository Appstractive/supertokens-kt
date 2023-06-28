package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CreateSessionRequest(
    val userId: String,
    val userDataInJWT: JsonElement? = null,
    val userDataInDatabase: JsonElement? = null,
    val enableAntiCsrf: Boolean = false,
    val useDynamicSigningKey: Boolean = false,
)
