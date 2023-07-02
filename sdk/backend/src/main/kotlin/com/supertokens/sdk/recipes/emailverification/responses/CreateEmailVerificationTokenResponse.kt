package com.supertokens.sdk.recipes.emailverification.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateEmailVerificationTokenResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val token: String,
): BaseResponse
