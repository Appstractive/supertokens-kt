package com.supertokens.sdk.core

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.core.requests.CreateJwtRequest
import com.supertokens.sdk.core.requests.DeleteUserRequest
import com.supertokens.sdk.core.responses.CreateJwtResponse
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
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

    suspend fun SuperTokens.getUserById(userId: String, recipeId: String = EmailPasswordRecipe.ID): User {

        val response = client.get {
            url {
                path(PATH_GET_USER)
                parameters.append("userId", userId)
            }
            header(Constants.HEADER_RECIPE_ID, recipeId)
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

    suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String): User {

        val response = client.get {
            url {
                path(PATH_GET_USER)
                parameters.append("phoneNumber", phoneNumber)
            }
            header(Constants.HEADER_RECIPE_ID, PasswordlessRecipe.ID)
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

    suspend fun SuperTokens.createJwt(
        issuer: String,
        validityInSeconds: Long = 86400L,
        useStaticSigningKey: Boolean = false,
        payload: Map<String, Any?>? = null
    ): String {
        val response = client.post(PATH_CREATE_JWT) {
            setBody(
                CreateJwtRequest(
                    jwksDomain = issuer,
                    validity = validityInSeconds,
                    useStaticSigningKey = useStaticSigningKey,
                    payload = payload?.toJsonElement() ?: JsonObject(emptyMap())
                )
            )
        }

        return response.parse<CreateJwtResponse, String> {
            checkNotNull(it.jwt)
        }
    }

    companion object {
        const val PATH_GET_USER = "/recipe/user"
        const val PATH_DELETE_USER = "/user/remove"
        const val PATH_CREATE_JWT = "/recipe/jwt"
    }

}

suspend fun SuperTokens.getUserById(userId: String, recipeId: String = EmailPasswordRecipe.ID): User = with(core) {
    return getUserById(userId, recipeId)
}

suspend fun SuperTokens.getUserByIdOrNull(userId: String): User? = runCatching {
    getUserById(userId, EmailPasswordRecipe.ID)
}.getOrNull() ?: runCatching {
    getUserById(userId, ThirdPartyRecipe.ID)
}.getOrNull() ?: runCatching {
    getUserById(userId, PasswordlessRecipe.ID)
}.getOrNull()

suspend fun SuperTokens.getUserByEMail(email: String, recipeId: String = EmailPasswordRecipe.ID): User = with(core) {
    return getUserByEMail(email, recipeId)
}

suspend fun SuperTokens.getUserByEMailOrNull(email: String): User? = runCatching {
    getUserByEMail(email, EmailPasswordRecipe.ID)
}.getOrNull() ?: runCatching {
    getUserByEMail(email, ThirdPartyRecipe.ID)
}.getOrNull() ?: runCatching {
    getUserByEMail(email, PasswordlessRecipe.ID)
}.getOrNull()

suspend fun SuperTokens.getUserByPhoneNumber(phoneNumber: String): User = with(core) {
    return getUserByPhoneNumber(phoneNumber)
}

suspend fun SuperTokens.getUserByPhoneNumberOrNull(phoneNumber: String): User? = runCatching {
    getUserByPhoneNumber(phoneNumber)
}.getOrNull()

suspend fun SuperTokens.deleteUser(userId: String): SuperTokensStatus = with(core) {
    return deleteUser(userId)
}

suspend fun SuperTokens.getJwks(): JsonObject = with(core) {
    return getJwks()
}

suspend fun SuperTokens.createJwt(
    issuer: String,
    validityInSeconds: Long = 86400L,
    useStaticSigningKey: Boolean = false,
    payload: Map<String, Any?>? = null
): String = with(core) {
    return createJwt(
        issuer = issuer,
        validityInSeconds = validityInSeconds,
        useStaticSigningKey = useStaticSigningKey,
        payload = payload,
    )
}