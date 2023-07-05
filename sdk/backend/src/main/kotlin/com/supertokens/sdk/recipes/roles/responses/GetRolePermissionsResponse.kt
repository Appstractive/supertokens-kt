package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetRolePermissionsResponse(
    override val status: String,
    val permissions: List<String>,
): BaseResponse
