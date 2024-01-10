package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequestDTO
import kotlinx.serialization.Serializable

@Deprecated("Use AuthorizationUrlResponseDTO instead")
typealias AuthorizationUrlResponse = AuthorizationUrlResponseDTO

@Serializable
data class AuthorizationUrlResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val urlWithQueryParams: String?,
    val pkceCodeVerifier: String? = null,
): BaseResponseDTO
