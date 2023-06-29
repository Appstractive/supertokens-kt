package com.supertokens.sdk.recipes.common

import com.supertokens.sdk.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    override val status: String,
    val user: User? = null,
): BaseResponse