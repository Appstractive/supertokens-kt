package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class GetRolePermissionsResponseDTO(
    override val status: String,
    val permissions: List<String>,
): BaseResponseDTO
