package com.supertokens.sdk.recipes.roles.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetRoleUsersResponse(
    override val status: String,
    val users: List<String>,
): BaseResponse
