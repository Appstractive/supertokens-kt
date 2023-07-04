package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SignInUpResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val createdNewUser: Boolean,
    val user: User,
): BaseResponse