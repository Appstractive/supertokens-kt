package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class SetUserRoleResponseDTO(
    override val status: String,
    val didUserAlreadyHaveRole: Boolean? = null,
): BaseResponseDTO
