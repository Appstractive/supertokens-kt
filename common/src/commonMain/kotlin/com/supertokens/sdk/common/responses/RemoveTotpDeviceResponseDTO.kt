package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class RemoveTotpDeviceResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val didDeviceExist: Boolean? = null,
) : BaseResponseDTO
