package com.supertokens.sdk.recipes.thirdparty.providers.gitlab

import kotlinx.serialization.Serializable

@Serializable
data class GitLabGetUserResponse(
    val id: Long,
    val email: String?,
)
