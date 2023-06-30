package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class FormField(
    val id: String,
    val value: String,
)

@Serializable
data class FormFieldRequest(
    val formFields: List<FormField>,
)
