package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class FormFieldDTO(
    val id: String,
    val value: String,
)

@Serializable
data class FormFieldRequestDTO(
    val formFields: List<FormFieldDTO>,
)
