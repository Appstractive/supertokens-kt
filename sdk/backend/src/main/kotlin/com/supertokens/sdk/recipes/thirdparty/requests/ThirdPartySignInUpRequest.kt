package com.supertokens.sdk.recipes.thirdparty.requests

import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyEmail(
    val id: String,
    val isVerified: Boolean,
)

@Serializable
data class ThirdPartySignInUpRequest(
    val thirdPartyId: String,
    val thirdPartyUserId: String,
    val email: ThirdPartyEmail,
)
