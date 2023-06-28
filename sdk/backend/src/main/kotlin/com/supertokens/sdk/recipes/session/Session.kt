package com.supertokens.sdk.recipes.session

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Session(
    val handle: String,
    val userId: String,
    val userDataInJWT: JsonObject?,
)