package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.thirdparty.providers.BuildProvider
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderBuilder
import com.supertokens.sdk.recipes.thirdparty.providers.ProviderConfig
import com.supertokens.sdk.recipes.thirdparty.requests.ThirdPartyEmail
import com.supertokens.sdk.recipes.thirdparty.requests.ThirdPartySignInUpRequest
import com.supertokens.sdk.recipes.thirdparty.responses.ThirdPartyGetUsersResponse
import com.supertokens.sdk.recipes.thirdparty.responses.ThirdPartySignInUpData
import com.supertokens.sdk.recipes.thirdparty.responses.ThirdPartySignInUpResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import it.czerwinski.kotlin.util.Either

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
    ): Either<SuperTokensStatus, ThirdPartySignInUpData> {
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

        return response.parse<ThirdPartySignInUpResponse, ThirdPartySignInUpData> {
            ThirdPartySignInUpData(
                createdNewUser = createdNewUser,
                user = user,
            )
        }
    }

    suspend fun getUsersByEmail(email: String): Either<SuperTokensStatus, List<User>> {
        val response = superTokens.client.get("$PATH_USERS_BY_EMAIL?email=$email") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<ThirdPartyGetUsersResponse, List<User>> {
            users
        }
    }

    fun getProvider(id: String) = providers.firstOrNull { it.id == id }

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