package com.supertokens.sdk.recipes.emailverification.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailTokenResponse(
    override val status: String,
    val userId: String,
    val email: String,
): BaseResponse
