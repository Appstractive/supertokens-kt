package com.supertokens.sdk.common.requests

import com.supertokens.sdk.common.responses.ThirdPartyTokenResponse
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartySignInUpRequest(
    val redirectURI: String,
    val thirdPartyId: String,
    val code: String?,
    val authCodeResponse: ThirdPartyTokenResponse?,
    val clientId: String?
)