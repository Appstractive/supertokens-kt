package com.supertokens.sdk.recipes.totp.responses

import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class RemoveTotpDeviceResponse(
    override val status: String,
    val didDeviceExist: Boolean?,
): BaseResponse
