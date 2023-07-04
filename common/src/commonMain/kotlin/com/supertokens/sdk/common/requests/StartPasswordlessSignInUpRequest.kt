package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class StartPasswordlessSignInUpRequest(
    val email: String? = null,
    val phoneNumber: String? = null,
)
