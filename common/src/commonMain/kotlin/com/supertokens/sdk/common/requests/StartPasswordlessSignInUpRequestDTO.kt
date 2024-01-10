package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Deprecated("Use StartPasswordlessSignInUpRequestDTO instead")
typealias StartPasswordlessSignInUpRequest = StartPasswordlessSignInUpRequestDTO

@Serializable
data class StartPasswordlessSignInUpRequestDTO(
    val email: String? = null,
    val phoneNumber: String? = null,
)
