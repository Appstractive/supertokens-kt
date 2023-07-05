package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SetUserRoleResponse(
    override val status: String,
    val didUserAlreadyHaveRole: Boolean,
): BaseResponse
