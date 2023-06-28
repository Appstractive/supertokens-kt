package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable

@Serializable
data class RefreshSessionRequest(
    val refreshToken: String,
    val enableAntiCsrf: Boolean = false,
    val antiCsrfToken: String? = null,
)
