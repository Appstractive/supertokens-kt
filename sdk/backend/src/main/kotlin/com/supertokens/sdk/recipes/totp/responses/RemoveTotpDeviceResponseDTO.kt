package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class RemoveTotpDeviceResponseDTO(
    override val status: String,
    val didDeviceExist: Boolean? = null,
): BaseResponseDTO
