package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpDeviceResponseDTO(
    override val status: String,
    val wasAlreadyVerified: Boolean? = null,
): BaseResponseDTO
