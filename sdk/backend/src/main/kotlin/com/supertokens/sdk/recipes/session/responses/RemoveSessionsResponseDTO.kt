package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class RemoveSessionsResponseDTO(
    override val status: String,
    val sessionHandlesRevoked: List<String>,
) : BaseResponseDTO
