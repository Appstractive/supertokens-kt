package com.supertokens.sdk.recipes.accountlinking.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreatePrimaryUserRequestDTO(
    val recipeUserId: String,
)
