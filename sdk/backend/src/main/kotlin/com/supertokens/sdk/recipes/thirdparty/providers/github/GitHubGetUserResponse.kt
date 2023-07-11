package com.supertokens.sdk.recipes.thirdparty.providers.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubGetUserResponse(
    val id: Long,
    val email: String?,
)
