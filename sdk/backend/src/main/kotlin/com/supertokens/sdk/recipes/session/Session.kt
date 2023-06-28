package com.supertokens.sdk.recipes.session

import com.supertokens.sdk.common.extractedContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Session(
    val handle: String,
    val userId: String,
    val userDataInJWT: JsonObject?,
)

data class SessionData(
    val handle: String,
    val userId: String,
    val userDataInJWT: Map<String, Any?>?,
)

fun Session.toData() = SessionData(
    handle = handle,
    userId = userId,
    userDataInJWT = userDataInJWT?.extractedContent
)