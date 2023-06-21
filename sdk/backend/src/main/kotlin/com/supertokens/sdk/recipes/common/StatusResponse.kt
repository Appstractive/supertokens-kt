package com.supertokens.sdk.recipes.common

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
)