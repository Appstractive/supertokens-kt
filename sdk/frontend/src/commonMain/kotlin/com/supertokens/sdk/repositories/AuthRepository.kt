package com.supertokens.sdk.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {

    data object Unauthenticated: AuthState()

    data class LoggedIn(val userId: String): AuthState()
    data class Authenticated(val userId: String): AuthState()

}

interface AuthRepository {

    val authState: StateFlow<AuthState>

    fun setAuthenticated(userId: String)
    fun setLoggedIn(userId: String)
    fun setUnauthenticated()

}

class AuthRepositoryImpl: AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState = _authState.asStateFlow()

    override fun setAuthenticated(userId: String) {
        _authState.value = AuthState.Authenticated(userId = userId)
    }

    override fun setLoggedIn(userId: String) {
        _authState.value = AuthState.LoggedIn(userId = userId)
    }

    override fun setUnauthenticated() {
        _authState.value = AuthState.Unauthenticated
    }

}