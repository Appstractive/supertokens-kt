package com.supertokens.sdk.recipes.emailverification.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailResponse(
    override val status: String,
    val isVerified: Boolean,
): BaseResponse
