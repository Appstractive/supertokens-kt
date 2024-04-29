package com.supertokens.sdk.repositories.user

import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface UserRepository {

    val claims: StateFlow<AccessTokenClaims?>

    suspend fun setClaims(claims: AccessTokenClaims?)
    suspend fun getClaims(): AccessTokenClaims?

    suspend fun clear() {
        setClaims(null)
    }

    suspend fun setClaimsFromJwt(jwt: String) {
        setClaims(jwt.parseJwtClaims())
    }

    suspend fun getUserId(): String? = getClaims()?.sub
    suspend fun getEmail(): String? = getClaims()?.email
    suspend fun isEmailVerified(): Boolean = getClaims()?.emailVerified == true
    suspend fun getRoles(): List<String> = getClaims()?.roles ?: emptyList()
    suspend fun getPermissions(): List<String> = getClaims()?.permissions ?: emptyList()
    suspend fun getFactors(): Map<String, Long> = getClaims()?.multiFactor?.factors ?: emptyMap()
    suspend fun isMultiFactorVerified(): Boolean = getClaims()?.multiFactor?.verified == true

}

@OptIn(ExperimentalEncodingApi::class)
fun String.parseJwtClaims(): AccessTokenClaims {
    val payload =
        Json.decodeFromString<AccessTokenClaims>(Base64.decode(split(".")[1]).decodeToString())
    return payload
}
