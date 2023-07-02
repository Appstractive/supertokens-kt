package com.supertokens.sdk.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyTokenResponse(
    val accessToken: String,
    val idToken: String? = null,
)