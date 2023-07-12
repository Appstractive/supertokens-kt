package com.supertokens.sdk.repositories.user

class UserRepositoryMemory: UserRepository {

    var claims: Map<String, Any?>? = null

    override suspend fun setClaims(claims: Map<String, Any?>?) {
        this.claims = claims
    }

    override suspend fun getClaims(): Map<String, Any?>? {
        return claims
    }

}