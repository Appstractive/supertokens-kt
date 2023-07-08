package com.supertokens.sdk.common.models

import kotlinx.serialization.Serializable

@Serializable
data class ThirdParty(
    val id: String,
    val userId: String,
)

@Serializable
data class User(
    val id: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val timeJoined: Long,
    val thirdParty: ThirdParty? = null,
)
