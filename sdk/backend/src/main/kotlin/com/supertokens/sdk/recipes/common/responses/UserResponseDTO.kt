package com.supertokens.sdk.recipes.common.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    override val status: String,
    val user: User? = null,
): BaseResponseDTO