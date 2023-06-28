package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.recipes.common.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithTokenResponse(
    override val status: String,
    val userId: String,
): BaseResponse