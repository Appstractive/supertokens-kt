package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Deprecated("Use ExistsResponseDTO instead")
typealias ExistsResponse = ExistsResponseDTO

@Serializable
data class ExistsResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val exists: Boolean,
) : BaseResponseDTO
