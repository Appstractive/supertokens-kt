package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_THIRD_PARTY
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.post
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.models.SignInUpData
import com.supertokens.sdk.recipes.thirdparty.providers.BuildProvider
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.requests.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.requests.ThirdPartySignInUpRequest
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.header
import io.ktor.client.request.setBody

fun <C : ProviderConfig, R : Provider<C>> ThirdPartyConfig.provider(
    builder: ProviderBuilder<C, R>,
    configure: C.() -> Unit = {}
) {
  +builder.install(configure)
}

class ThirdPartyConfig : RecipeConfig {

  var providers: List<BuildProvider> = emptyList()
    private set

  operator fun BuildProvider.unaryPlus() {
    providers = providers + this
  }
}

class ThirdPartyRecipe(
    private val superTokens: SuperTokens,
    config: ThirdPartyConfig,
) : Recipe<ThirdPartyConfig> {

  val providers: List<Provider<*>> = config.providers.map { it.invoke(superTokens, this) }

  /** Signin/up a user */
  @Throws(SuperTokensStatusException::class)
  suspend fun signInUp(
      thirdPartyId: String,
      thirdPartyUserId: String,
      email: String,
      isVerified: Boolean,
      tenantId: String?,
  ): SignInUpData {
    Result
    val response =
        superTokens.post(PATH_SIGN_IN_UP, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_THIRD_PARTY)

          setBody(
              ThirdPartySignInUpRequest(
                  thirdPartyId = thirdPartyId,
                  thirdPartyUserId = thirdPartyUserId,
                  email =
                      ThirdPartyEmail(
                          id = email,
                          isVerified = isVerified,
                      ),
              ),
          )
        }

    return response
        .parse<SignInUpResponseDTO, SignInUpData> {
          SignInUpData(
              user = checkNotNull(it.user), createdNewUser = checkNotNull(it.createdNewUser))
        }
        .also {
          if (it.createdNewUser) {
            superTokens._events.tryEmit(SuperTokensEvent.UserSignUp(it.user, RECIPE_THIRD_PARTY))
          } else {
            superTokens._events.tryEmit(SuperTokensEvent.UserSignIn(it.user, RECIPE_THIRD_PARTY))
          }
        }
  }

  fun getProvider(id: String, clientType: String? = null): Provider<*>? =
      getProvidersById(id = id).let {
        it.firstOrNull { provider -> provider.clientType == clientType }
            ?: it.firstOrNull { provider -> provider.isDefault }
            ?: it.firstOrNull()
      }

  fun getProviderById(id: String): Provider<*>? =
      getProvidersById(id = id).let {
        it.firstOrNull { provider -> provider.isDefault } ?: it.firstOrNull()
      }

  fun getProvidersById(id: String): List<Provider<*>> = providers.filter { it.id == id }

  companion object {
    const val PATH_SIGN_IN_UP = "/recipe/signinup"
  }
}

val ThirdParty =
    object : RecipeBuilder<ThirdPartyConfig, ThirdPartyRecipe>() {

      override fun install(
          configure: ThirdPartyConfig.() -> Unit
      ): (SuperTokens) -> ThirdPartyRecipe {
        val config = ThirdPartyConfig().apply(configure)

        return { ThirdPartyRecipe(it, config) }
      }
    }

/** Signin/up a user */
suspend fun SuperTokens.thirdPartySignInUp(
    thirdPartyId: String,
    thirdPartyUserId: String,
    email: String,
    isVerified: Boolean,
    tenantId: String? = null,
) =
    getRecipe<ThirdPartyRecipe>()
        .signInUp(
            thirdPartyId = thirdPartyId,
            thirdPartyUserId = thirdPartyUserId,
            email = email,
            isVerified = isVerified,
            tenantId = tenantId,
        )
