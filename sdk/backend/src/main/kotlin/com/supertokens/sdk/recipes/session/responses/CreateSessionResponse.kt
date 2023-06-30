package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.models.Token
import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.models.SessionData
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionResponse(
    override val status: String,
    val session: SessionResponse,
    val accessToken: Token,
    val refreshToken: Token,
    val antiCsrfToken: String?,
): BaseResponse
