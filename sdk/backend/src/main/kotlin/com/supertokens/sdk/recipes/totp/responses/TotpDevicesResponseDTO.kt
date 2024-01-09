package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class TotpDeviceReponse(
    val name: String,
    val period: Int,
    val skew: Long,
    val verified: Boolean,
)

@Serializable
data class TotpDevicesResponseDTO(
    override val status: String,
    val devices: List<TotpDeviceReponse>? = null,
): BaseResponseDTO
