package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class RemoveUserRoleResponseDTO(
    override val status: String,
    val didUserHaveRole: Boolean? = null,
): BaseResponseDTO
