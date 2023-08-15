package com.supertokens.sdk.core

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.core.requests.DeleteUserRequest
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import kotlinx.serialization.json.JsonObject

internal class CoreHandler {

    suspend fun SuperTokens.getUserById(userId: String): User {

        val response = client.get {
            url {
                path(PATH_GET_USER)
                parameters.append("userId", userId)
            }
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.getUserByEMail(email: String, recipeId: String = EmailPasswordRecipe.ID): User {

        val response = client.get {
            url {
                path(PATH_GET_USER)
                parameters.append("email", email)
            }
            header(Constants.HEADER_RECIPE_ID, recipeId)
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String, recipeId: String = PasswordlessRecipe.ID): User {

        val response = client.get {
            url {
                path(PATH_GET_USER)
                parameters.append("phoneNumber", phoneNumber)
            }
            header(Constants.HEADER_RECIPE_ID, recipeId)
        }

        return response.parseUser()
    }

    suspend fun SuperTokens.deleteUser(userId: String): SuperTokensStatus {
        val response = client.post(PATH_DELETE_USER) {
            setBody(
                DeleteUserRequest(
                    userId = userId,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun SuperTokens.getJwks(): JsonObject {
        val response = client.get("/.well-known/jwks.json")

        return response.body()
    }

    companion object {
        const val PATH_GET_USER = "/recipe/user"
        const val PATH_DELETE_USER = "/user/remove"
    }

}

suspend fun SuperTokens.getUserById(userId: String): User = with(core) {
    return getUserById(userId)
}

suspend fun SuperTokens.getUserByIdOrNull(userId: String): User? = with(core) {
    return runCatching {
        getUserById(userId)
    }.getOrNull()
}

suspend fun SuperTokens.getUserByEMail(email: String, recipeId: String = EmailPasswordRecipe.ID): User = with(core) {
    return getUserByEMail(email, recipeId)
}

suspend fun SuperTokens.getUserByEMailOrNull(email: String, recipeId: String = EmailPasswordRecipe.ID): User? = with(core) {
    return runCatching {
        getUserByEMail(email, recipeId)
    }.getOrNull()
}

suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String, recipeId: String = PasswordlessRecipe.ID): User = with(core) {
    return getUserByPhoneNumber(phoneNumber, recipeId)
}

suspend fun SuperTokens.getUserByPhoneNumberOrNull(phoneNumber: String, recipeId: String = PasswordlessRecipe.ID): User? = with(core) {
    return runCatching {
        getUserByPhoneNumber(phoneNumber, recipeId)
    }.getOrNull()
}

suspend fun SuperTokens.deleteUser(userId: String): SuperTokensStatus = with(core) {
    return deleteUser(userId)
}

suspend fun SuperTokens.getJwks(): JsonObject = with(core) {
    return getJwks()
}