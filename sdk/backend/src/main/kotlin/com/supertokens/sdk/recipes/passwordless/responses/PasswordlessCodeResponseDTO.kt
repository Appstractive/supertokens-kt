package com.supertokens.sdk.recipes.passwordless.responses

import com.supertokens.sdk.common.responses.BaseResponseDTO
import kotlinx.serialization.Serializable

@Serializable
data class PasswordlessCodeResponseDTO(
    override val status: String,
    val preAuthSessionId: String? = null,
    val codeId: String? = null,
    val deviceId: String? = null,
    val userInputCode: String? = null,
    val linkCode: String? = null,
    val timeCreated: Long? = null,
    val codeLifetime: Long? = null,
) : BaseResponseDTO
