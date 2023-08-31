package com.supertokens.sdk.core.responses

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateJwtResponse(
    override val status: String = SuperTokensStatus.OK.value,
    val jwt: String? = null,
): BaseResponse
