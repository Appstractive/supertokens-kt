package com.supertokens.sdk.recipes.thirdparty.providers.github

import kotlinx.serialization.Serializable

@Serializable
data class GithubGetEmailsResponse(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
)
