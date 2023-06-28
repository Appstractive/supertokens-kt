package com.supertokens.sdk.recipes.session.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerifySessionRequest(
    val accessToken: String,
    val enableAntiCsrf: Boolean = false,
    val doAntiCsrfCheck: Boolean = false,
    val checkDatabase: Boolean = false,
    val antiCsrfToken: String? = null,
)
