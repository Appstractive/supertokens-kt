package com.supertokens.sdk.recipes.passwordless.models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordlessCodeData(
    val preAuthSessionId: String,
    val codeId: String,
    val deviceId: String,
    val userInputCode: String,
    val linkCode: String,
    val timeCreated: Long,
    val codeLifetime: Long,
)
