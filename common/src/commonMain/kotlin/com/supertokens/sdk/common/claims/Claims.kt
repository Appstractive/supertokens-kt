package com.supertokens.sdk.common.claims

import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.util.StringListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimMFA(
    @SerialName(Claims.MFA_FACTORS)
    val factors: Map<String, Long>,
    @SerialName(Claims.MFA_VERIFIED)
    val verified: Boolean,
)

@Serializable
data class AccessTokenClaims(
    @SerialName(Claims.EXPIRES_AT)
    val expiresAt: Long,
    @SerialName(Claims.ISSUED_AT)
    val issuedAt: Long,
    @SerialName(Claims.USER_ID)
    val sub: String,
    @SerialName(Claims.ISSUER)
    val issuer: String,
    @SerialName(Claims.AUDIENCE)
    @Serializable(with = StringListSerializer::class)
    val audience: List<String>? = null,
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
