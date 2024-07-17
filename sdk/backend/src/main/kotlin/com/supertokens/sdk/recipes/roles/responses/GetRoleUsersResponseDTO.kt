package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class GetRoleUsersResponseDTO(
    override val status: String,
    val users: List<String> = emptyList(),
) : BaseResponseDTO
