package com.supertokens.sdk.core.requests

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserRequest(
    val userId: String,
)
