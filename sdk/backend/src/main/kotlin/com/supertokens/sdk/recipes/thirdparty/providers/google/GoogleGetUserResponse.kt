package com.supertokens.sdk.recipes.thirdparty.providers.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleGetUserResponse(
    val id: String,
    val email: String?,
    val verified_email: Boolean,
)
