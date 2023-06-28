package com.supertokens.sdk.recipes.thirdparty.providers.github

import kotlinx.serialization.Serializable

@Serializable
data class GithubGetEmailResponse(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
)
