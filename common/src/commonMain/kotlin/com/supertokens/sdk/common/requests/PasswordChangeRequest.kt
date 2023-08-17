package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class PasswordChangeRequest(
    val formFields: List<FormField>,
)
