package com.supertokens.sdk.recipes.session.models

data class GetSessionData(
    val userDataInDatabase: Map<String, Any?>? = null,
    val userDataInJWT: Map<String, Any?>? = null,
    val userId: String,
    val expiry: Long,
    val timeCreated: Long,
    val sessionHandle: String,
)
