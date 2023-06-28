package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.recipes.common.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateResetPasswordTokenResponse(
    override val status: String,
    val token: String,
): BaseResponse