package com.supertokens.sdk.recipes.accountlinking.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class UnlinkAccountsResponseDTO(
    override val status: String,
    val wasRecipeUserDeleted: Boolean,
    val wasLinked: Boolean,
) : BaseResponseDTO
