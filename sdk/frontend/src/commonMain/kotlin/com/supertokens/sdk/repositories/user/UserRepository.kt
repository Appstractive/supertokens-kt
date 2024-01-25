package com.supertokens.sdk.repositories.user

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.extractedContent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface UserRepository {

    val userId: StateFlow<String?>
    val roles: StateFlow<Set<String>>
    val permissions: StateFlow<Set<String>>
    val email: StateFlow<String?>
    val emailVerified: StateFlow<Boolean>

    suspend fun setClaims(claims: Map<String, Any?>?)
    suspend fun getClaims(): Map<String, Any?>?

    suspend fun clear() {
        setClaims(null)
    }

    suspend fun setClaimsFromJwt(jwt: String) {
        setClaims(jwt.parseJwtClaims())
    }

    suspend fun getUserId(): String? = getClaims().getUserID()
    suspend fun getEmail(): String? = getClaims().getEmail()
    suspend fun isEmailVerified(): Boolean = getClaims().getEmailVerified()
    suspend fun getRoles(): Set<String> = getClaims().getRoles()
    suspend fun getPermissions(): Set<String> = getClaims().getPermissions()

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

internal fun Map<String, Any?>?.getUserID(): String? {
    return this?.get(Claims.USER_ID)?.toString()
}

internal fun Map<String, Any?>?.getEmail(): String? {
    return this?.get(Claims.EMAIL)?.toString()
}

internal fun Map<String, Any?>?.getEmailVerified(): Boolean {
    return this?.get(Claims.EMAIL_VERIFIED) == true
}

internal fun Map<String, Any?>?.getRoles(): Set<String> {
    return (this?.get(Claims.ROLES) as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
}

internal fun Map<String, Any?>?.getPermissions(): Set<String> {
    return (this?.get(Claims.PERMISSIONS) as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
}