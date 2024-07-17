package com.supertokens.sdk.models

data class TotpDevice(
    val name: String,
    val period: Long,
    val skew: Long,
    val verified: Boolean,
)
