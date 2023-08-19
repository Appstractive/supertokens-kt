package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val message: String? = null,
): BaseResponse