package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequestDTO(
    val method: String,
    val token: String?,
    val formFields: List<FormFieldDTO>,
)
