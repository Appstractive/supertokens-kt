package com.supertokens.sdk.recipes.thirdparty.providers.facebook

import kotlinx.serialization.Serializable

@Serializable
data class FacebookGetUserResponse(
    val id: String,
    val email: String,
)
