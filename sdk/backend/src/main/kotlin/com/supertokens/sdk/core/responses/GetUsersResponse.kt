package com.supertokens.sdk.core.responses

import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class GetUsersResponse(
    override val status: String,
    val users: List<User>? = null,
): BaseResponse
