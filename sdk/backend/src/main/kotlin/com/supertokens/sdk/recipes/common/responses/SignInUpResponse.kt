package com.supertokens.sdk.recipes.common.responses

import com.supertokens.sdk.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SignInUpResponse(
    override val status: String,
    val createdNewUser: Boolean,
    val user: User,
): BaseResponse