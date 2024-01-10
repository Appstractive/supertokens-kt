package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Deprecated("Use StatusResponseDTO instead")
typealias StatusResponse = StatusResponseDTO

@Serializable
data class StatusResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val message: String? = null,
): BaseResponseDTO