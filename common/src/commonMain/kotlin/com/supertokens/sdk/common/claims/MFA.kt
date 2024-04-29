package com.supertokens.sdk.common.claims

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimMFA(
    @SerialName("c")
    val factors: Map<String, Long>,
    @SerialName("v")
    val verified: Boolean,
)