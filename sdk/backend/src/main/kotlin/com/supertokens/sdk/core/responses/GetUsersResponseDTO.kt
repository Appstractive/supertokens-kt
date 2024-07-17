package com.supertokens.sdk.core.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class GetUsersResponseDTO(
    override val status: String,
    val users: List<User>? = null,
) : BaseResponseDTO
