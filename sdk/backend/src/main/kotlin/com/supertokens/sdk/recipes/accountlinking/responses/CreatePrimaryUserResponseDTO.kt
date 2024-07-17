package com.supertokens.sdk.recipes.accountlinking.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CreatePrimaryUserResponseDTO(
    override val status: String,
    val wasAlreadyAPrimaryUser: Boolean,
    val user: User,
) : BaseResponseDTO
