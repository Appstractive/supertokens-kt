package com.supertokens.sdk.repositories.user

import kotlinx.coroutines.flow.MutableStateFlow

class UserRepositoryMemory: UserRepository {

    private var myClaims: Map<String, Any?> = emptyMap()

    override val userId: MutableStateFlow<String?> = MutableStateFlow(null)
    override val roles: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val permissions: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val email: MutableStateFlow<String?> = MutableStateFlow(null)
    override val emailVerified: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun setClaims(claims: Map<String, Any?>?) {
        myClaims = claims ?: emptyMap()

        userId.value = claims.getUserID()
        roles.value = claims.getRoles()
        permissions.value = claims.getPermissions()
        email.value = claims.getEmail()
        emailVerified.value = claims.getEmailVerified()
    }

    override suspend fun getClaims(): Map<String, Any?> {
        return myClaims
    }

}