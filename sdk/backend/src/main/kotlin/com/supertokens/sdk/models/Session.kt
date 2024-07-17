package com.supertokens.sdk.models

data class SessionData(
    val handle: String,
    val userId: String,
    val userDataInJWT: Map<String, Any?>?,
)

data class CreateSessionData(
    val session: SessionData,
    val accessToken: Token,
    val refreshToken: Token,
    val antiCsrfToken: String?,
)

data class RegenerateSessionData(
    val session: SessionData,
    val accessToken: Token,
)
