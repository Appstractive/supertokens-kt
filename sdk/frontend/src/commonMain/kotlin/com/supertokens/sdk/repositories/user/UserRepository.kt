package com.supertokens.sdk.repositories.user

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.extractedContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface UserRepository {

    suspend fun setClaims(claims: Map<String, Any?>?)
    suspend fun getClaims(): Map<String, Any?>?

    suspend fun clear() {
        setClaims(null)
    }

    suspend fun setClaimsFromJwt(jwt: String) {
        setClaims(jwt.parseJwtClaims())
    }

    suspend fun getUserId(): String? = getClaims()?.get(Claims.USER_ID)?.toString()
    suspend fun getEmail(): String? = getClaims()?.get(Claims.EMAIL)?.toString()
    suspend fun isEmailVerified(): Boolean = getClaims()?.get(Claims.EMAIL_VERIFIED) != false

}

@OptIn(ExperimentalEncodingApi::class)
fun String.parseJwtClaims(): Map<String, Any?> {
    val payload = Json.decodeFromString<JsonObject>(Base64.decode(split(".")[1]).decodeToString())
    return payload.extractedContent
}

suspend fun SuperTokensClient.getUserId(): String? {
    return userRepository.getUserId()
}

suspend fun SuperTokensClient.getEmail(): String? {
    return userRepository.getEmail()
}

suspend fun SuperTokensClient.isEmailVerified(): Boolean {
    return userRepository.isEmailVerified()
}