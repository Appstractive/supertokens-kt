package com.supertokens.sdk.recipes.emailverification.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmailVerificationTokenResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val token: String? = null,
): BaseResponseDTO
