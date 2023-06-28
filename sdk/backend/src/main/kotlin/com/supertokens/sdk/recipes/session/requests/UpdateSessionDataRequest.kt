package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UpdateSessionDataRequest(
    val sessionHandle: String,
    val userDataInDatabase: JsonElement?,
)
