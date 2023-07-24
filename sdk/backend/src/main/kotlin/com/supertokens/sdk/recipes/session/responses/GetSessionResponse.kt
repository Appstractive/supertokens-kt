package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GetSessionResponse(
    override val status: String,
    val userDataInDatabase: JsonObject? = null,
    val userDataInJWT: JsonObject? = null,
    val userId: String? = null,
    val expiry: Long? = null,
    val timeCreated: Long? = null,
    val sessionHandle: String? = null,
): BaseResponse