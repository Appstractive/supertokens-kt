package com.supertokens.sdk.recipes.sessions.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {

  val isLoggedIn: Boolean
    get()= this !is Unauthenticated

  // no local accesstoken saved
  data object Unauthenticated : AuthState()

  // local refreshtoken exists, but not authenticated against backend yet
  data class LoggedIn(val userId: String) : AuthState()

  // accestoken acquired
  data class Authenticated(val userId: String, val multiFactorVerified: Boolean) : AuthState()
}

interface AuthRepository {

  val authState: StateFlow<AuthState>

  fun setAuthenticated(userId: String, multiFactorVerified: Boolean = false)

  fun setLoggedIn(userId: String)

  fun setUnauthenticated()
}

class AuthRepositoryImpl : AuthRepository {

  private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
  override val authState = _authState.asStateFlow()

  override fun setAuthenticated(userId: String, multiFactorVerified: Boolean) {
    _authState.value =
        AuthState.Authenticated(
            userId = userId,
            multiFactorVerified = multiFactorVerified,
        )
  }

  override fun setLoggedIn(userId: String) {
    _authState.value = AuthState.LoggedIn(userId = userId)
  }

  override fun setUnauthenticated() {
    _authState.value = AuthState.Unauthenticated
  }
}
