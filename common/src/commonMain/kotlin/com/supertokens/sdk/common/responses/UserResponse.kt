package com.supertokens.sdk.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val timeJoined: Long,
)
