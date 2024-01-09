package com.supertokens.sdk.common.requests

import com.supertokens.sdk.common.responses.ThirdPartyTokensDTO
import kotlinx.serialization.Serializable

@Serializable
data class RedirectUriInfoDTO(
    val redirectURIOnProviderDashboard: String,
    val redirectURIQueryParams: Map<String, String>,
    val pkceCodeVerifier: String?,
)

@Serializable
data class ThirdPartySignInUpRequestDTO(
    val thirdPartyId: String,
    val redirectURIInfo: RedirectUriInfoDTO? = null,
    val oAuthTokens: ThirdPartyTokensDTO? = null,
    val clientType: String? = null,
)