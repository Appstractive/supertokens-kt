package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class GetRolesResponseDTO(
    override val status: String,
    val roles: List<String>,
): BaseResponseDTO
