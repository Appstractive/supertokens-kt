package com.supertokens.sdk.recipes.sessions.repositories

import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.StringFormat

interface ClaimsRepository {

  val decoder: StringFormat

  val claims: StateFlow<AccessTokenClaims?>

  suspend fun setClaims(claims: String?)

  suspend fun getClaims(): AccessTokenClaims?

  suspend fun clear() {
    setClaims(null)
  }

  @OptIn(ExperimentalEncodingApi::class)
  suspend fun setClaimsFromJwt(jwt: String) {
    setClaims(Base64.decode(jwt.split(".")[1]).decodeToString())
  }

  suspend fun getUserId(): String? = getClaims()?.sub

  suspend fun getEmail(): String? = getClaims()?.email

  suspend fun isEmailVerified(): Boolean = getClaims()?.emailVerified == true

  suspend fun getRoles(): List<String> = getClaims()?.roles ?: emptyList()

  suspend fun getPermissions(): List<String> = getClaims()?.permissions ?: emptyList()

  suspend fun getFactors(): Map<String, Long> = getClaims()?.multiFactor?.factors ?: emptyMap()

  suspend fun isMultiFactorVerified(): Boolean =
      getClaims()?.multiFactor == null || getClaims()?.multiFactor?.verified == true
}
