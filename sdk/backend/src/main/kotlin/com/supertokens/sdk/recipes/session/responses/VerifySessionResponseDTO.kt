package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import com.supertokens.sdk.models.Token
import kotlinx.serialization.Serializable

@Serializable
data class VerifySessionResponseDTO(
    override val status: String,
    val session: SessionResponse? = null,
    val accessToken: Token? = null,
) : BaseResponseDTO
