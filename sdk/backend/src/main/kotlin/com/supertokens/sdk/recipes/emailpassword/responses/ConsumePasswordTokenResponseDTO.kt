package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class ConsumePasswordTokenResponseDTO(
    override val status: String,
    val userId: String? = null,
    val email: String? = null,
) : BaseResponseDTO
