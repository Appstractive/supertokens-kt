package com.supertokens.sdk.recipes.accountlinking.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class LinkAccountsResponseDTO(
    override val status: String,
    val accountsAlreadyLinked: Boolean,
    val user: User,
): BaseResponseDTO
