package com.supertokens.sdk.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val timeJoined: Long,
)
