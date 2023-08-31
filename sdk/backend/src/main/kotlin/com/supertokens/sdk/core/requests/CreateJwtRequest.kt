package com.supertokens.sdk.core.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CreateJwtRequest(
    val algorithm: String = "RS256",
    val jwksDomain: String,
    val validity: Long,
    val useStaticSigningKey: Boolean,
    val payload: JsonObject,
)
