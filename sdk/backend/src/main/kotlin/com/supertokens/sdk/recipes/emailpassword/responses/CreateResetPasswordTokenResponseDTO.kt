package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CreateResetPasswordTokenResponseDTO(
    override val status: String,
    val token: String? = null,
) : BaseResponseDTO
