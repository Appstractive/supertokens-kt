package com.supertokens.sdk.recipes.usermetadata.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserMetaDataResponse(
    override val status: String,
    val metadata: JsonObject? = null,
): BaseResponse
