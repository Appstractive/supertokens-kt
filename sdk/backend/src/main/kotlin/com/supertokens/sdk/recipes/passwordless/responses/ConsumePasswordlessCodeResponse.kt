package com.supertokens.sdk.recipes.passwordless.responses

import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Serializable
data class ConsumePasswordlessCodeResponse(
    override val status: String,
    val createdNewUser: Boolean? = null,
    val user: User? = null,
    val failedCodeInputAttemptCount: Int? = null,
    val maximumCodeInputAttempts: Int? = null,
): BaseResponse
