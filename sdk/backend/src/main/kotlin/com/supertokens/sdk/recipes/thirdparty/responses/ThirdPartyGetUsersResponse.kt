package com.supertokens.sdk.recipes.thirdparty.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyGetUsersResponse(
    override val status: String,
    val users: List<User>,
): BaseResponse
