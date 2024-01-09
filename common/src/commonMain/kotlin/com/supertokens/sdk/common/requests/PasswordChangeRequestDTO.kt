package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class PasswordChangeRequestDTO(
    val formFields: List<FormFieldDTO>,
)
