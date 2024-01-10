package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Deprecated("Use VerifyEmailResponseDTO instead")
typealias VerifyEmailResponse = VerifyEmailResponseDTO

@Serializable
data class VerifyEmailResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val isVerified: Boolean?,
): BaseResponseDTO
