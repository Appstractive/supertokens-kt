package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.models.User
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
import com.supertokens.sdk.recipes.thirdparty.responses.ThirdPartyGetUsersResponse
import com.supertokens.sdk.recipes.common.responses.SignInUpResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

fun <C: ProviderConfig, R: Provider<C>> ThirdPartyConfig.provider(builder: ProviderBuilder<C, R>, configure: C.() -> Unit = {}) {
    +builder.install(configure)
}

class ThirdPartyConfig: RecipeConfig {

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

    suspend fun signInUp(
        thirdPartyId: String,
        thirdPartyUserId: String,
        email: String
    ): SignInUpData {
        Result
        val response = superTokens.client.post(PATH_SIGN_IN_UP) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ThirdPartySignInUpRequest(
                    thirdPartyId = thirdPartyId,
                    thirdPartyUserId = thirdPartyUserId,
                    email = ThirdPartyEmail(
                        id = email,
                    ),
                )
            )
        }

        return response.parse<SignInUpResponse, SignInUpData> {
            SignInUpData(
                user = it.user,
                createdNewUser = it.createdNewUser
            )
        }
    }

    suspend fun getUsersByEmail(email: String): List<User> {
        val response = superTokens.client.get("$PATH_USERS_BY_EMAIL?email=$email") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<ThirdPartyGetUsersResponse, List<User>> {
            it.users
        }
    }

    fun getProvider(id: String): Provider<*>? = providers.filter { it.id == id }.let {
        it.firstOrNull { provider ->
            provider.isDefault
        } ?: it.firstOrNull()
    }

    companion object {
        const val ID = "thirdparty"

        const val PATH_SIGN_IN_UP = "recipe/signinup"
        const val PATH_USERS_BY_EMAIL = "recipe/users/by-email"
    }

}

val ThirdParty = object: RecipeBuilder<ThirdPartyConfig, ThirdPartyRecipe>() {

    override fun install(configure: ThirdPartyConfig.() -> Unit): (SuperTokens) -> ThirdPartyRecipe {
        val config = ThirdPartyConfig().apply(configure)

        return {
            ThirdPartyRecipe(it, config)
        }
    }

}

suspend fun SuperTokens.thirdPartySignInUp(
    thirdPartyId: String,
    thirdPartyUserId: String,
    email: String,
) = getRecipe<ThirdPartyRecipe>().signInUp(thirdPartyId, thirdPartyUserId, email)

suspend fun SuperTokens.getUsersByEmail(
    email: String,
) = getRecipe<ThirdPartyRecipe>().getUsersByEmail(email)