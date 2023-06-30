package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class RemoveSessionsResponse(
    override val status: String,
    val sessionHandlesRevoked: List<String>,
): BaseResponse
