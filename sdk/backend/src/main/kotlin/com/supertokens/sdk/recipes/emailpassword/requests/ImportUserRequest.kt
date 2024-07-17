package com.supertokens.sdk.recipes.emailpassword.requests

import kotlinx.serialization.Serializable

@Serializable
data class ImportUserRequest(
    val email: String,
    val passwordHash: String,
    val hashingAlgorithm: String,
)
