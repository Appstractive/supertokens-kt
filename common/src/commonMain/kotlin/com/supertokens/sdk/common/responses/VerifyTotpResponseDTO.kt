package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val currentNumberOfFailedAttempts: Long? = null,
    val maxNumberOfFailedAttempts: Long? = null,
    val retryAfterMs: Long? = null,
): BaseResponseDTO