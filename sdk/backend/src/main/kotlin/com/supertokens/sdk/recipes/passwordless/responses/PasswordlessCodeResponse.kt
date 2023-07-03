package com.supertokens.sdk.recipes.passwordless.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class PasswordlessCodeResponse(
    override val status: String,
    val preAuthSessionId: String,
    val codeId: String,
    val deviceId: String,
    val userInputCode: String,
    val linkCode: String,
    val timeCreated: Long,
    val codeLifetime: Long,
): BaseResponse
