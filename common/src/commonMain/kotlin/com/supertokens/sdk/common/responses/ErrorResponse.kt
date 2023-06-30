package com.supertokens.sdk.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: String = "Unexpected Error",
    val message: String,
)
