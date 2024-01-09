package com.supertokens.sdk.core.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CreateJwtResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val jwt: String? = null,
): BaseResponseDTO
