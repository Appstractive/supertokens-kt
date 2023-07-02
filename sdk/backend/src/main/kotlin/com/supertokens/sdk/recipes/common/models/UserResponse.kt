package com.supertokens.sdk.recipes.common.models

import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    override val status: String,
    val user: User? = null,
): BaseResponse