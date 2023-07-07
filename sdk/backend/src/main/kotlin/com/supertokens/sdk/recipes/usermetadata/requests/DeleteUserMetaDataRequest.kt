package com.supertokens.sdk.recipes.usermetadata.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserMetaDataRequest(
    val userId: String,
)
