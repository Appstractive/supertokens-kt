package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTotpDeviceResponse(
    override val status: String,
    val wasAlreadyVerified: Boolean? = null,
): BaseResponse
