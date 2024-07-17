package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class ImportTotpDeviceResponseDTO(
    override val status: String,
    val deviceName: String,
) : BaseResponseDTO
