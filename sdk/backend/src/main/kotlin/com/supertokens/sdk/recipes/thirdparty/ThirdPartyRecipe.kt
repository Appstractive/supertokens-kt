package com.supertokens.sdk.recipes.thirdparty

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.Recipe
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

class ThirdPartyRecipe : Recipe {

    suspend fun signInUp(
        superTokens: SuperTokens,
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

    suspend fun getUsersByEmail(superTokens: SuperTokens, email: String): Either<SuperTokensStatus, List<User>> {
        val response = superTokens.client.get("$PATH_USERS_BY_EMAIL?email=$email") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<ThirdPartyGetUsersResponse, List<User>> {
            users
        }
    }

    companion object {
        const val ID = "thirdparty"

        const val PATH_SIGN_IN_UP = "recipe/signinup"
        const val PATH_USERS_BY_EMAIL = "recipe/users/by-email"
    }

}

fun SuperTokensConfig.thirdParty(init: ThirdPartyRecipe.() -> Unit) {
    val recipe = ThirdPartyRecipe()
    recipe.init()
    +recipe
}

suspend fun SuperTokens.thirdPartySignInUp(
    thirdPartyId: String,
    thirdPartyUserId: String,
    email: String,
) = getRecipe<ThirdPartyRecipe>().signInUp(this, thirdPartyId, thirdPartyUserId, email)

suspend fun SuperTokens.getUsersByEmail(
    email: String,
) = getRecipe<ThirdPartyRecipe>().getUsersByEmail(this, email)