package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizationUrlResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val urlWithQueryParams: String?,
    val pkceCodeVerifier: String? = null,
): BaseResponseDTO
