package com.supertokens.sdk.recipes.common

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    override val status: String,
): BaseResponse