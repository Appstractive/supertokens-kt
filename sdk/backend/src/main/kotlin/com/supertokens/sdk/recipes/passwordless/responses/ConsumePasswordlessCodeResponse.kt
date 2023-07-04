package com.supertokens.sdk.recipes.passwordless.responses

import com.supertokens.sdk.common.responses.BaseResponse
import com.supertokens.sdk.common.models.User
import kotlinx.serialization.Serializable

@Serializable
data class ConsumePasswordlessCodeResponse(
    override val status: String,
    val createdNewUser: Boolean?,
    val user: User?,
    val failedCodeInputAttemptCount: Int?,
    val maximumCodeInputAttempts: Int?,
): BaseResponse
