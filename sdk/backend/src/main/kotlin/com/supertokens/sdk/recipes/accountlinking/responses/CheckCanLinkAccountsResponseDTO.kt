package com.supertokens.sdk.recipes.accountlinking.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class CheckCanLinkAccountsResponseDTO(
    override val status: String,
    val accountsAlreadyLinked: Boolean,
) : BaseResponseDTO
