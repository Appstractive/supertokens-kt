package com.supertokens.sdk.common.claims

import com.supertokens.sdk.common.Claims
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimMFA(
    @SerialName("c")
    val factors: Map<String, Long>,
    @SerialName("v")
    val verified: Boolean,
)

@Serializable
data class AccessTokenClaims(
    @SerialName(Claims.USER_ID)
    val sub: String,
    @SerialName(Claims.ISSUER)
    val issuer: String,
    @SerialName(Claims.AUDIENCE)
    val audience: String? = null,
    @SerialName(Claims.EMAIL)
    val email: String? = null,
    @SerialName(Claims.EMAIL_VERIFIED)
    val emailVerified: Boolean? = null,
    @SerialName(Claims.PHONE_NUMBER)
    val phoneNumber: String? = null,
    @SerialName(Claims.ROLES)
    val roles: List<String>? = null,
    @SerialName(Claims.PERMISSIONS)
    val permissions: List<String>? = null,
    @SerialName(Claims.MFA)
    val multiFactor: ClaimMFA? = null
)
