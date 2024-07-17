package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateTotpDeviceResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val deviceName: String? = null,
    val qrCodeString: String? = null,
    val secret: String? = null,
) : BaseResponseDTO
