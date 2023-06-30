package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.models.Token
import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.recipes.session.Session
import com.supertokens.sdk.recipes.session.SessionData
import kotlinx.serialization.Serializable

@Serializable
data class VerifySessionResponse(
    override val status: String,
    val session: Session?,
    val accessToken: Token?,
): BaseResponse

data class VerifySessionData(
    val session: SessionData,
    val accessToken: Token?,
)