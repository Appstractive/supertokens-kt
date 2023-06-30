package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val method: String,
    val token: String?,
    val formFields: List<FormField>,
)
