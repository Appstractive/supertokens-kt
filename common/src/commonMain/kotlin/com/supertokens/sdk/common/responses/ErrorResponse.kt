package com.supertokens.sdk.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    override val status: String = "UNKNOWN_ERROR",
    val message: String,
): BaseResponse
