package com.supertokens.sdk.recipes.session

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String,
    val expiry: Long,
    val createdTime: Long,
)