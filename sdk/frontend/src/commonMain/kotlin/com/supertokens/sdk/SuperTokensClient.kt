package com.supertokens.sdk

import com.russhwolf.settings.Settings
import com.supertokens.sdk.recipes.BuildRecipe
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.core.respositories.UserRepository
import com.supertokens.sdk.recipes.core.respositories.UserRepositoryImpl
import com.supertokens.sdk.recipes.sessions.SessionRecipe
import com.supertokens.sdk.recipes.sessions.repositories.AuthRepository
import com.supertokens.sdk.recipes.sessions.repositories.AuthState
import com.supertokens.sdk.recipes.sessions.repositories.ClaimsRepository
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

internal expect fun getDefaultSettings(): Settings

@SuperTokensDslMarker
class SuperTokensClientConfig(
    val apiBaseUrl: String,
) {

  var tenantId: String? = null

  /**
   * HTTP client used by the SDK (be sure to install
   * [ContentNegotiation](https://ktor.io/docs/client-serialization.html) with json support).
   *
   * @see <a href="https://ktor.io/docs/client-serialization.html">ContentNegotiation</a>
   */
  @OptIn(ExperimentalSerializationApi::class)
  var client: HttpClient =
      HttpClient(CIO) {
        install(ContentNegotiation) {
          json(
              Json {
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
                ignoreUnknownKeys = true
              },
          )
        }
      }

  var clientName: String = "MyMobileApp"

  var userRepository: UserRepository? = null

  var settings: Settings? = null

  val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  var recipes: List<BuildRecipe> = emptyList()
    private set

  operator fun BuildRecipe.unaryPlus() {
    recipes = recipes + this
  }

  fun <C : RecipeConfig, R : Recipe<C>> recipe(
      builder: RecipeBuilder<C, R>,
      configure: C.() -> Unit = {}
  ) {
    +builder.install(configure)
  }
}

class SuperTokensClient(
    private val config: SuperTokensClientConfig,
) {

  internal val scope: CoroutineScope
    get() = config.scope

  val tenantId: String?
    get() = config.tenantId

  val settings: Settings by lazy { config.settings ?: getDefaultSettings() }

  val recipes: List<Recipe<*>> = config.recipes.map { it.invoke(this) }

  val apiClient by lazy {
    config.client.config {
      recipes.forEach { with(it) { configureClient() } }

      defaultRequest {
        url(config.apiBaseUrl)
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Origin, config.clientName)
      }
    }
  }

  val authRepository: AuthRepository
    get() = getRecipe<SessionRecipe>().authRepository

  val userRepository: UserRepository by lazy {
    config.userRepository
        ?: UserRepositoryImpl(
            settings = settings,
        )
  }

  val tokenRepository: TokensRepository
    get() = getRecipe<SessionRecipe>().tokensRepository

  val claimsRepository: ClaimsRepository
    get() = getRecipe<SessionRecipe>().claimsRepository

  private val _isInitialized = MutableStateFlow(false)
  val isInitialized = _isInitialized.asStateFlow()

  inline fun <reified T : Recipe<*>> getRecipe(): T =
      recipes.filterIsInstance<T>().firstOrNull()
          ?: throw RuntimeException("Recipe ${T::class.simpleName} not configured")

  inline fun <reified T : Recipe<*>> hasRecipe(): Boolean =
      recipes.filterIsInstance<T>().isNotEmpty()

  /**
   * true, if the the user is at least logged in (but may not be authenticated from the backend yet)
   * It essentially means, there is a refresh token present, but no access token yet, e.g. during
   * startup when a new access token hasn't been fetched yet.
   */
  fun isLoggedIn(): Boolean = authRepository.authState.value !is AuthState.Unauthenticated

  // true, if the user was authenticated from the backend (an access token is present)
  fun isAuthenticated(): Boolean = authRepository.authState.value is AuthState.Authenticated

  init {
    scope.launch {
      recipes.forEach { recipe -> recipe.postInit() }

      _isInitialized.value = true
    }
  }
}

fun superTokensClient(
    apiBaseUrl: String,
    init: SuperTokensClientConfig.() -> Unit = {}
): SuperTokensClient {
  val config =
      SuperTokensClientConfig(
              apiBaseUrl = apiBaseUrl,
          )
          .apply(init)
  return SuperTokensClient(config)
}
