package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.recipes.common.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsResponse(
    override val status: String,
    val sessionHandles: List<String>,
): BaseResponse
