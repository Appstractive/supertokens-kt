package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.recipes.session.Session
import com.supertokens.sdk.recipes.session.Token
import kotlinx.serialization.Serializable

@Serializable
data class VerifySessionResponse(
    val status: String,
    val session: Session?,
    val accessToken: Token?,
)

@Serializable
data class VerifySessionData(
    val session: Session,
    val accessToken: Token?,
)