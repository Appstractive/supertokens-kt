package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import kotlinx.serialization.Serializable

@Serializable
data class StartPasswordlessSignInUpResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val deviceId: String,
    val preAuthSessionId: String,
    val flowType: PasswordlessMode,
): BaseResponse
