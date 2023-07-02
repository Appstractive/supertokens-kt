package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GetSessionResponse(
    override val status: String,
    val userDataInDatabase: JsonObject?,
    val userDataInJWT: JsonObject?,
    val userId: String,
    val expiry: Long,
    val timeCreated: Long,
    val sessionHandle: String,
): BaseResponse