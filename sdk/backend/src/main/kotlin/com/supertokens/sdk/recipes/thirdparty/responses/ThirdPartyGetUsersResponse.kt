package com.supertokens.sdk.recipes.thirdparty.responses

import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.common.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyGetUsersResponse(
    override val status: String,
    val users: List<User>,
): BaseResponse
