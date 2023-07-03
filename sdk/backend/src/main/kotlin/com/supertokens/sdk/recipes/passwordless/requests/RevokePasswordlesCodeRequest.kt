package com.supertokens.sdk.recipes.passwordless.requests

import kotlinx.serialization.Serializable

@Serializable
data class RevokePasswordlesCodeRequest(
    val codeId: String,
)
