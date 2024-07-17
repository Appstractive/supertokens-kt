package com.supertokens.sdk.recipes.accountlinking.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CheckPrimaryUserResponseDTO(
    override val status: String,
    val wasAlreadyAPrimaryUser: Boolean,
) : BaseResponseDTO
