package com.supertokens.sdk.recipes.usermetadata.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UpdateUserMetaDataRequest(
    val userId: String,
    val metadataUpdate: JsonElement,
)
