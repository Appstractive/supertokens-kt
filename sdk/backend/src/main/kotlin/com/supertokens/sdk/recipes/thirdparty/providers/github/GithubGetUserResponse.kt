package com.supertokens.sdk.recipes.thirdparty.providers.github

import kotlinx.serialization.Serializable

@Serializable
data class GithubGetUserResponse(
    val id: Long,
    val email: String?,
)
