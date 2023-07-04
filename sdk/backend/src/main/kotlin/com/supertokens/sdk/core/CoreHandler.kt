package com.supertokens.sdk.core

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.net.URLEncoder

internal class CoreHandler {

    suspend fun SuperTokens.getUserById(userId: String): User {

        val response = client.get("${EmailPasswordRecipe.PATH_GET_USER}?userId=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(userId, "UTF-8")
            }
        }")

        return response.parseUser()
    }

    suspend fun SuperTokens.getUserByEMail(email: String, recipeId: String = EmailPasswordRecipe.ID): User {

        val response = client.get("${EmailPasswordRecipe.PATH_GET_USER}?email=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(email, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, recipeId)
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String, recipeId: String = PasswordlessRecipe.ID): User {

        val response = client.get("${EmailPasswordRecipe.PATH_GET_USER}?phoneNumber=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(phoneNumber, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, recipeId)
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.getJwks(): JsonObject {
        val response = client.get("/.well-known/jwks.json")

        return response.body()
    }

}

suspend fun SuperTokens.getUserById(userId: String): User = with(core) {
    return getUserById(userId)
}

suspend fun SuperTokens.getUserByEMail(email: String, recipeId: String = EmailPasswordRecipe.ID): User = with(core) {
    return getUserByEMail(email, recipeId)
}

suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String, recipeId: String = PasswordlessRecipe.ID): User = with(core) {
    return getUserByPhoneNumber(phoneNumber, recipeId)
}

suspend fun SuperTokens.getJwks(): JsonObject = with(core) {
    return getJwks()
}