package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class TotpDeviceDTO(
    val name: String,
    val period: Long,
    val skew: Long,
    val verified: Boolean,
)

@Serializable
data class GetTotpDevicesResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val devices: List<TotpDeviceDTO>? = null,
): BaseResponseDTO
