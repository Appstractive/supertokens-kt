package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class GetSessionsResponseDTO(
    override val status: String,
    val sessionHandles: List<String>,
) : BaseResponseDTO
