package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class AddTotpDeviceResponseDTO(
    override val status: String,
    val secret: String? = null,
) : BaseResponseDTO
