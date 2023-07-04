package com.supertokens.sdk.common.responses

import com.supertokens.sdk.common.SuperTokensStatus
import kotlinx.serialization.Serializable

@Serializable
data class ExistsResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val exists: Boolean,
): BaseResponse
