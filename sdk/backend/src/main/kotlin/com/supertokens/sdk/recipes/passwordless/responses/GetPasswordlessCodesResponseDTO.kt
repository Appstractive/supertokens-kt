package com.supertokens.sdk.recipes.passwordless.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class PasswordLessCode(
    val codeId: String,
    val timeCreated: Long,
    val codeLifetime: Long,
)

@Serializable
data class PasswordlessDevices(
    val preAuthSessionId: String,
    val failedCodeInputAttemptCount: Int,
    val email: String?,
    val phoneNumber: String?,
    val codes: List<PasswordLessCode>,
)

@Serializable
data class GetPasswordlessCodesResponseDTO(
    override val status: String,
    val devices: List<PasswordlessDevices>,
) : BaseResponseDTO
