package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import com.supertokens.sdk.models.Token
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionResponseDTO(
    override val status: String,
    val session: SessionResponse? = null,
    val accessToken: Token? = null,
    val refreshToken: Token? = null,
    val antiCsrfToken: String? = null,
) : BaseResponseDTO
