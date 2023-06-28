package com.supertokens.sdk.recipes.thirdparty.responses

import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.common.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartySignInUpResponse(
    override val status: String,
    val createdNewUser: Boolean,
    val user: User,
): BaseResponse

data class ThirdPartySignInUpData(
    val createdNewUser: Boolean,
    val user: User,
)