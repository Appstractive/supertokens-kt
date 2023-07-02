package com.supertokens.sdk.common.requests

import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartySignInUpRequest(
    val redirectURI: String? = null,
    val thirdPartyId: String,
    val code: String? = null,
    val authCodeResponse: ThirdPartyTokenResponse? = null,
    val clientId: String? = null,
)