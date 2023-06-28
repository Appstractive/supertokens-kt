package com.supertokens.sdk.recipes.session

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.session.requests.CreateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RefreshSessionRequest
import com.supertokens.sdk.recipes.session.requests.RegenerateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RemoveSessionsRequest
import com.supertokens.sdk.recipes.session.requests.UpdateJwtDataRequest
import com.supertokens.sdk.recipes.session.requests.UpdateSessionDataRequest
import com.supertokens.sdk.recipes.session.requests.VerifySessionRequest
import com.supertokens.sdk.recipes.session.responses.CreateSessionData
import com.supertokens.sdk.recipes.session.responses.CreateSessionResponse
import com.supertokens.sdk.recipes.session.responses.GetSessionData
import com.supertokens.sdk.recipes.session.responses.GetSessionResponse
import com.supertokens.sdk.recipes.session.responses.GetSessionsResponse
import com.supertokens.sdk.recipes.session.responses.RegenerateSessionData
import com.supertokens.sdk.recipes.session.responses.RegenerateSessionResponse
import com.supertokens.sdk.recipes.session.responses.RemoveSessionsResponse
import com.supertokens.sdk.recipes.session.responses.VerifySessionData
import com.supertokens.sdk.recipes.session.responses.VerifySessionResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import it.czerwinski.kotlin.util.Either
import it.czerwinski.kotlin.util.Right

class SessionRecipe : Recipe {

    suspend fun createSession(
        superTokens: SuperTokens,
        userId: String,
        userDataInJWT: Map<String, Any?>? = null,
        userDataInDatabase: Map<String, Any?>? = null,
        enableAntiCsrf: Boolean = false,
        useDynamicSigningKey: Boolean = false,
    ): Either<SuperTokensStatus, CreateSessionData> {
        val response = superTokens.client.post(PATH_SESSION) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateSessionRequest(
                    userId = userId,
                    userDataInJWT = userDataInJWT?.toJsonElement(),
                    userDataInDatabase = userDataInDatabase?.toJsonElement(),
                    enableAntiCsrf = enableAntiCsrf,
                    useDynamicSigningKey = useDynamicSigningKey,
                )
            )
        }

        return response.parse<CreateSessionResponse, CreateSessionData> {
            CreateSessionData(
                session = session.toData(),
                accessToken = accessToken,
                refreshToken = refreshToken,
                antiCsrfToken = antiCsrfToken,
            )
        }
    }

    suspend fun getSession(superTokens: SuperTokens, sessionHandle: String): Either<SuperTokensStatus, GetSessionData> {
        val response = superTokens.client.get("$PATH_SESSION?sessionHandle=$sessionHandle") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetSessionResponse, GetSessionData> {
            GetSessionData(
                userId = userId,
                expiry = expiry,
                timeCreated = timeCreated,
                sessionHandle = sessionHandle,
                userDataInDatabase = userDataInDatabase?.entries?.associate {
                    it.key to it.value.extractedContent
                },
                userDataInJWT = userDataInJWT?.entries?.associate {
                    it.key to it.value.extractedContent
                },
            )
        }
    }

    suspend fun getSessions(superTokens: SuperTokens, userId: String): Either<SuperTokensStatus, List<String>> {
        val response = superTokens.client.get("$PATH_SESSIONS?userId=$userId") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetSessionsResponse, List<String>> {
            sessionHandles
        }
    }

    suspend fun removeSessions(superTokens: SuperTokens, sessionHandles: List<String>): Either<SuperTokensStatus, List<String>> {
        if (sessionHandles.isEmpty()) {
            return Right(emptyList())
        }

        val response = superTokens.client.post(PATH_SESSION_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RemoveSessionsRequest(
                    sessionHandles = sessionHandles,
                )
            )
        }

        return response.parse<RemoveSessionsResponse, List<String>> {
            sessionHandlesRevoked
        }
    }

    suspend fun verifySession(
        superTokens: SuperTokens,
        accessToken: String,
        enableAntiCsrf: Boolean = false,
        doAntiCsrfCheck: Boolean = false,
        checkDatabase: Boolean = false,
        antiCsrfToken: String? = null,
    ): Either<SuperTokensStatus, VerifySessionData> {

        val response = superTokens.client.post(PATH_SESSION_VERIFY) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                VerifySessionRequest(
                    accessToken = accessToken,
                    enableAntiCsrf = enableAntiCsrf,
                    doAntiCsrfCheck = doAntiCsrfCheck,
                    checkDatabase = checkDatabase,
                    antiCsrfToken = antiCsrfToken,
                )
            )
        }

        return response.parse<VerifySessionResponse, VerifySessionData> {
            VerifySessionData(
                session = session?.toData() ?: throw RuntimeException("VerifySession returned OK but no session"),
                accessToken = this.accessToken,
            )
        }
    }

    suspend fun refreshSession(
        superTokens: SuperTokens,
        refreshToken: String,
        enableAntiCsrf: Boolean = false,
        antiCsrfToken: String? = null,
    ): Either<SuperTokensStatus, CreateSessionData> {
        val response = superTokens.client.post(PATH_SESSION_REFRESH) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RefreshSessionRequest(
                    refreshToken = refreshToken,
                    enableAntiCsrf = enableAntiCsrf,
                    antiCsrfToken = antiCsrfToken,
                )
            )
        }

        return response.parse<CreateSessionResponse, CreateSessionData> {
            CreateSessionData(
                session = session.toData(),
                accessToken = accessToken,
                refreshToken = this.refreshToken,
                antiCsrfToken = antiCsrfToken,
            )
        }
    }

    suspend fun regenerateSession(
        superTokens: SuperTokens,
        accessToken: String,
        userDataInJWT: Map<String, Any?>? = null,
    ): Either<SuperTokensStatus, RegenerateSessionData> {

        val response = superTokens.client.post(PATH_SESSION_REGENERATE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RegenerateSessionRequest(
                    accessToken = accessToken,
                    userDataInJWT = userDataInJWT?.toJsonElement(),
                )
            )
        }

        return response.parse<RegenerateSessionResponse, RegenerateSessionData> {
            RegenerateSessionData(
                session = session?.toData() ?: throw RuntimeException("RegenerateSession returned OK but no session"),
                accessToken = this.accessToken ?: throw RuntimeException("RegenerateSession returned OK but no accessToken"),
            )
        }
    }

    suspend fun updateSessionData(superTokens: SuperTokens, sessionHandle: String, userDataInDatabase: Map<String, Any?>): SuperTokensStatus {
        val response = superTokens.client.put(PATH_SESSION_DATA) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UpdateSessionDataRequest(
                    sessionHandle = sessionHandle,
                    userDataInDatabase = userDataInDatabase.toJsonElement(),
                )
            )
        }

        return response.parse()
    }

    suspend fun updateJwtData(superTokens: SuperTokens, sessionHandle: String, userDataInJWT: Map<String, Any?>): SuperTokensStatus {
        val response = superTokens.client.put(PATH_JWT_DATA) {

            header("rid", ID)

            setBody(
                UpdateJwtDataRequest(
                    sessionHandle = sessionHandle,
                    userDataInJWT = userDataInJWT.toJsonElement(),
                )
            )
        }

        return response.parse()
    }

    companion object {
        const val ID = "session"

        const val PATH_SESSION = "recipe/session"
        const val PATH_SESSION_REMOVE = "recipe/session/remove"
        const val PATH_SESSION_VERIFY = "recipe/session/verify"
        const val PATH_SESSION_REFRESH = "recipe/session/refresh"
        const val PATH_SESSION_REGENERATE = "recipe/session/regenerate"
        const val PATH_SESSION_DATA = "recipe/session/data"
        const val PATH_JWT_DATA = "recipe/jwt/data"
        const val PATH_SESSIONS = "recipe/session/user"
    }

}

fun SuperTokensConfig.session(init: SessionRecipe.() -> Unit) {
    val recipe = SessionRecipe()
    recipe.init()
    +recipe
}

suspend fun SuperTokens.createSession(
    userId: String,
    userDataInJWT: Map<String, Any?>? = emptyMap(),
    userDataInDatabase: Map<String, Any?>? = emptyMap(),
    enableAntiCsrf: Boolean = false,
    useDynamicSigningKey: Boolean = false,
) = getRecipe<SessionRecipe>().createSession(this, userId, userDataInJWT, userDataInDatabase, enableAntiCsrf, useDynamicSigningKey)

suspend fun SuperTokens.getSession(
    sessionHandle: String,
) = getRecipe<SessionRecipe>().getSession(this, sessionHandle)

suspend fun SuperTokens.getSessions(
    userId: String,
) = getRecipe<SessionRecipe>().getSessions(this, userId)

suspend fun SuperTokens.removeSessions(
    sessionHandles: List<String>,
) = getRecipe<SessionRecipe>().removeSessions(this, sessionHandles)

suspend fun SuperTokens.verifySession(
    accessToken: String,
    enableAntiCsrf: Boolean = false,
    doAntiCsrfCheck: Boolean = false,
    checkDatabase: Boolean = false,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().verifySession(this, accessToken, enableAntiCsrf, doAntiCsrfCheck, checkDatabase, antiCsrfToken)

suspend fun SuperTokens.refreshSession(
    refreshToken: String,
    enableAntiCsrf: Boolean = false,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().refreshSession(this, refreshToken, enableAntiCsrf, antiCsrfToken)

suspend fun SuperTokens.regenerateSession(
    accessToken: String,
    userDataInJWT: Map<String, Any?>? = null,
) = getRecipe<SessionRecipe>().regenerateSession(this, accessToken, userDataInJWT)

suspend fun SuperTokens.updateSessionData(
    accessToken: String,
    userDataInDatabase: Map<String, Any?>,
) = getRecipe<SessionRecipe>().updateSessionData(this, accessToken, userDataInDatabase)

suspend fun SuperTokens.updateJwtData(
    accessToken: String,
    userDataInJWT: Map<String, Any?>,
) = getRecipe<SessionRecipe>().updateJwtData(this, accessToken, userDataInJWT)