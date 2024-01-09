package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class StartPasswordlessSignInUpRequestDTO(
    val email: String? = null,
    val phoneNumber: String? = null,
)
