package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use PasswordChangeRequestDTO instead")
typealias PasswordChangeRequest = PasswordChangeRequestDTO

@Serializable
data class PasswordChangeRequestDTO(
    val formFields: List<FormFieldDTO>,
)
