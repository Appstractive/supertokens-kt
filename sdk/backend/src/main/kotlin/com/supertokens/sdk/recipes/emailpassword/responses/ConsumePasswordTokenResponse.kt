package com.supertokens.sdk.recipes.emailpassword.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class ConsumePasswordTokenResponse(
    override val status: String,
    val userId: String? = null,
    val email: String? = null,
): BaseResponse