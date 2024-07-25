package com.supertokens.sdk.recipes.core.respositories

import com.russhwolf.settings.Settings
import com.supertokens.sdk.common.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface UserRepository {
  val user: StateFlow<User?>

  fun updateUser(user: User?)
  fun getUser(): User?
}

@OptIn(ExperimentalSerializationApi::class)
class UserRepositoryImpl(
    private val settings: Settings,
    private val json: Json = Json {
      isLenient = true
      explicitNulls = false
      encodeDefaults = true
      ignoreUnknownKeys = true
    },
): UserRepository {

  override val user: MutableStateFlow<User?> = MutableStateFlow(null)

  init {
    updateUser(getUser())
  }

  override fun updateUser(user: User?) {
    this.user.value = user
    if(user != null) {
      settings.putString(KEY_USER, json.encodeToString(user))
    }
    else {
      settings.remove(KEY_USER)
    }
  }

  override fun getUser(): User? = settings.getStringOrNull(KEY_USER)?.let { json.decodeFromString(it) }

  companion object {
    private const val KEY_USER = "user"
  }
}
