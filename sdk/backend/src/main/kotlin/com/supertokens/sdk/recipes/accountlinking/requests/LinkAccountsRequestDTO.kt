package com.supertokens.sdk.recipes.accountlinking.requests

import kotlinx.serialization.Serializable

@Serializable
data class LinkAccountsRequestDTO(
    val primaryUserId: String,
    val recipeUserId: String,
)
