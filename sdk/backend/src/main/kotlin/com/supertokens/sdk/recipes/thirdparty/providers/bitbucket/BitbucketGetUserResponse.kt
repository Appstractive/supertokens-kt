package com.supertokens.sdk.recipes.thirdparty.providers.bitbucket

import kotlinx.serialization.Serializable

@Serializable
data class BitbucketGetUserResponse(
    val uuid: String,
    val email: String?,
)
