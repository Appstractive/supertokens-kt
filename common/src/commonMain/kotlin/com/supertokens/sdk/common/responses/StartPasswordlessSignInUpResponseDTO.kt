package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import kotlinx.serialization.Serializable

@Serializable
data class StartPasswordlessSignInUpResponseDTO(
    override val status: String = SuperTokensStatus.OK.value,
    val deviceId: String? = null,
    val preAuthSessionId: String? = null,
    val flowType: PasswordlessMode? = null,
): BaseResponseDTO
