package com.supertokens.sdk.models

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String,
    val expiry: Long,
    val createdTime: Long,
)