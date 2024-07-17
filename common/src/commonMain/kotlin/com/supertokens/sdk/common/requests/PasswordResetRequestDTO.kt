package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use PasswordResetRequestDTO instead")
typealias PasswordResetRequest = PasswordResetRequestDTO

@Serializable
data class PasswordResetRequestDTO(
    val method: String = "token",
    val token: String?,
    val formFields: List<FormFieldDTO>,
)
