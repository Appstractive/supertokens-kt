package com.supertokens.sdk.recipes.usermetadata.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserMetaDataResponseDTO(
    override val status: String,
    val metadata: JsonObject? = null,
) : BaseResponseDTO
