package com.supertokens.sdk.repositories.user

import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class UserRepositoryMemory: UserRepository {

    override val claims: MutableStateFlow<AccessTokenClaims?> = MutableStateFlow(null)

    override val decoder: Json = Json {
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun setClaims(claims: AccessTokenClaims?) {
        this.claims.value = claims
    }

    override suspend fun getClaims(): AccessTokenClaims? {
        return claims.value
    }

}