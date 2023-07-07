package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class AddTotpDeviceResponse(
    override val status: String,
    val secret: String?,
): BaseResponse
