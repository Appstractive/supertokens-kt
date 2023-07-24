package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithTokenResponse(
    override val status: String,
    val userId: String? = null,
): BaseResponse