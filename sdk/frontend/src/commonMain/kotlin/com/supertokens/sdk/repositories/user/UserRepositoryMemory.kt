package com.supertokens.sdk.repositories.user

import com.supertokens.sdk.common.claims.AccessTokenClaims
import kotlinx.coroutines.flow.MutableStateFlow

class UserRepositoryMemory: UserRepository {

    override val claims: MutableStateFlow<AccessTokenClaims?> = MutableStateFlow(null)

    override suspend fun setClaims(claims: AccessTokenClaims?) {
        this.claims.value = claims
    }

    override suspend fun getClaims(): AccessTokenClaims? {
        return claims.value
    }

}