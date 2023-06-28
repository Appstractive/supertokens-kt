package com.supertokens.sdk.recipes.session.responses

import com.supertokens.sdk.recipes.session.Session
import com.supertokens.sdk.recipes.session.Token
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionResponse(
    val status: String,
    val session: Session,
    val accessToken: Token,
    val refreshToken: Token,
    val antiCsrfToken: String?,
)

@Serializable
data class CreateSessionData(
    val session: Session,
    val accessToken: Token,
    val refreshToken: Token,
    val antiCsrfToken: String?,
)