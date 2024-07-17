package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use FormFieldDTO instead")
typealias FormField = FormFieldDTO

@Deprecated("Use FormFieldRequestDTO instead")
typealias FormFieldRequest = FormFieldRequestDTO

@Serializable
data class FormFieldDTO(
    val id: String,
    val value: String,
)

@Serializable
data class FormFieldRequestDTO(
    val formFields: List<FormFieldDTO>,
)
