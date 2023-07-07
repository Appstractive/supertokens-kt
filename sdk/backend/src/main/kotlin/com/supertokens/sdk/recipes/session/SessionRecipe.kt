package com.supertokens.sdk.recipes.session

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.models.CreateSessionData
import com.supertokens.sdk.models.RegenerateSessionData
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.recipes.CustomJwtData
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.session.models.GetSessionData
import com.supertokens.sdk.recipes.session.models.VerifySessionData
import com.supertokens.sdk.recipes.session.requests.CreateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RefreshSessionRequest
import com.supertokens.sdk.recipes.session.requests.RegenerateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RemoveSessionsRequest
import com.supertokens.sdk.recipes.session.requests.UpdateJwtDataRequest
import com.supertokens.sdk.recipes.session.requests.UpdateSessionDataRequest
import com.supertokens.sdk.recipes.session.requests.VerifySessionRequest
import com.supertokens.sdk.recipes.session.responses.CreateSessionResponse
import com.supertokens.sdk.recipes.session.responses.GetSessionResponse
import com.supertokens.sdk.recipes.session.responses.GetSessionsResponse
import com.supertokens.sdk.recipes.session.responses.RegenerateSessionResponse
import com.supertokens.sdk.recipes.session.responses.RemoveSessionsResponse
import com.supertokens.sdk.recipes.session.responses.VerifySessionResponse
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.toData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody


class SessionConfig: RecipeConfig {

    // if true, attach tokens to response headers
    var headerBasedSessions: Boolean = true

    // if true, set tokens to cookies
    var cookieBasedSessions: Boolean = true

    var secureCookies: Boolean = false

    // the JWT issuer to use
    var issuer: String? = null

    internal var customJwtData: CustomJwtData? = null

    // attach any additional data to Jwts on session creation and regeneration
    fun customJwtData(jwtData: CustomJwtData) {
        customJwtData = jwtData
    }

}

class SessionRecipe(
    private val superTokens: SuperTokens,
    private val config: SessionConfig
) : Recipe<SessionConfig> {

    // if true, attach tokens to response headers
    val headerBasedSessions = config.headerBasedSessions

    // if true, set tokens to cookies
    val cookieBasedSessions = config.cookieBasedSessions

    val secureCookies by lazy {
        superTokens.appConfig.api.scheme == "https"
    }

    val cookieSameSite by lazy {
        // will be 'none' if https is used and frontend and api are different hosts, else 'lax'
        if(
            secureCookies &&
            (superTokens.appConfig.frontends.size > 1 ||
                    superTokens.appConfig.frontends.first().host != superTokens.appConfig.api.host)
        )
            "none"
        else
            "lax"
    }

    // attach any additional data to Jwts on session creation and regeneration
    val customJwtData: CustomJwtData? = config.customJwtData

    // the JWT issuer to use
    val issuer by lazy {
        config.issuer ?: superTokens.appConfig.api.host
    }

    suspend fun getJwtData(user: User): Map<String, Any?> = buildMap {
        set("iss", issuer)
        set("aud", superTokens.appConfig.frontends.map { it.host })

        buildMap {
            user.email?.let {
                set("email", it)
            }
            user.phoneNumber?.let {
                set("phoneNumber", it)
            }
        }

        superTokens.recipes.forEach {
            it.getExtraJwtData(user).forEach { entry ->
                set(entry.key, entry.value)
            }
        }
        customJwtData?.let {
            it.invoke(superTokens, user).forEach { entry ->
                set(entry.key, entry.value)
            }
        }
    }

    /**
     * Create a new Session
     */
    suspend fun createSession(
        userId: String,
        userDataInJWT: Map<String, Any?> = emptyMap(),
        userDataInDatabase: Map<String, Any?> = emptyMap(),
        enableAntiCsrf: Boolean = false,
        useDynamicSigningKey: Boolean = false,
    ): CreateSessionData {
        val response = superTokens.client.post(PATH_SESSION) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateSessionRequest(
                    userId = userId,
                    userDataInJWT = userDataInJWT.toJsonElement(),
                    userDataInDatabase = userDataInDatabase.toJsonElement(),
                    enableAntiCsrf = enableAntiCsrf,
                    useDynamicSigningKey = useDynamicSigningKey,
                )
            )
        }

        return response.parse<CreateSessionResponse, CreateSessionData> {
            CreateSessionData(
                session = it.session.toData(),
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                antiCsrfToken = it.antiCsrfToken,
            )
        }
    }

    /**
     * Get user and session information for a given session handle
     */
    suspend fun getSession(sessionHandle: String): GetSessionData {
        val response = superTokens.client.get("$PATH_SESSION?sessionHandle=$sessionHandle") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetSessionResponse, GetSessionData> {
            GetSessionData(
                userId = it.userId,
                expiry = it.expiry,
                timeCreated = it.timeCreated,
                sessionHandle = sessionHandle,
                userDataInDatabase = it.userDataInDatabase?.entries?.associate { entry ->
                    entry.key to entry.value.extractedContent
                },
                userDataInJWT = it.userDataInJWT?.entries?.associate { entry ->
                    entry.key to entry.value.extractedContent
                },
            )
        }
    }

    /**
     * Get session handles for a user
     */
    suspend fun getSessions(userId: String): List<String> {
        val response = superTokens.client.get("$PATH_SESSIONS?userId=$userId") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetSessionsResponse, List<String>> {
            it.sessionHandles
        }
    }

    /**
     * Delete a sesion
     */
    suspend fun removeSessions(sessionHandles: List<String>): List<String> {
        if (sessionHandles.isEmpty()) {
            return emptyList()
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
            it.sessionHandlesRevoked
        }
    }

    /**
     * Verify a Session
     */
    suspend fun verifySession(
        accessToken: String,
        enableAntiCsrf: Boolean = false,
        doAntiCsrfCheck: Boolean = false,
        checkDatabase: Boolean = false,
        antiCsrfToken: String? = null,
    ): VerifySessionData {

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
                session = it.session?.toData() ?: throw RuntimeException("VerifySession returned OK but no session"),
                accessToken = it.accessToken,
            )
        }
    }

    /**
     * Refresh a Session
     */
    suspend fun refreshSession(
        refreshToken: String,
        enableAntiCsrf: Boolean = false,
        antiCsrfToken: String? = null,
    ): CreateSessionData {
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
                session = it.session.toData(),
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                antiCsrfToken = it.antiCsrfToken,
            )
        }
    }

    /**
     * Regenerate a session
     */
    suspend fun regenerateSession(
        accessToken: String,
        userDataInJWT: Map<String, Any?>? = null,
    ): RegenerateSessionData {

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
                session = it.session?.toData() ?: throw RuntimeException("RegenerateSession returned OK but no session"),
                accessToken = it.accessToken ?: throw RuntimeException("RegenerateSession returned OK but no accessToken"),
            )
        }
    }

    /**
     * Change session data
     */
    suspend fun updateSessionData(sessionHandle: String, userDataInDatabase: Map<String, Any?>): SuperTokensStatus {
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

    /**
     * Change JWT data for a session
     */
    suspend fun updateJwtData(sessionHandle: String, userDataInJWT: Map<String, Any?>): SuperTokensStatus {
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

val Sessions = object: RecipeBuilder<SessionConfig, SessionRecipe>() {

    override fun install(configure: SessionConfig.() -> Unit): (SuperTokens) -> SessionRecipe {
        val config = SessionConfig().apply(configure)

        return {
            SessionRecipe(it, config)
        }
    }

}

/**
 * Create a new Session
 */
suspend fun SuperTokens.createSession(
    userId: String,
    userDataInJWT: Map<String, Any?> = emptyMap(),
    userDataInDatabase: Map<String, Any?> = emptyMap(),
    enableAntiCsrf: Boolean = false,
    useDynamicSigningKey: Boolean = false,
) = getRecipe<SessionRecipe>().createSession(userId, userDataInJWT, userDataInDatabase, enableAntiCsrf, useDynamicSigningKey)

/**
 * Get user and session information for a given session handle
 */
suspend fun SuperTokens.getSession(
    sessionHandle: String,
) = getRecipe<SessionRecipe>().getSession(sessionHandle)

/**
 * Get session handles for a user
 */
suspend fun SuperTokens.getSessions(
    userId: String,
) = getRecipe<SessionRecipe>().getSessions(userId)

/**
 * Delete a sesion
 */
suspend fun SuperTokens.removeSessions(
    sessionHandles: List<String>,
) = getRecipe<SessionRecipe>().removeSessions(sessionHandles)

/**
 * Verify a Session
 */
suspend fun SuperTokens.verifySession(
    accessToken: String,
    enableAntiCsrf: Boolean = false,
    doAntiCsrfCheck: Boolean = false,
    checkDatabase: Boolean = false,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().verifySession(accessToken, enableAntiCsrf, doAntiCsrfCheck, checkDatabase, antiCsrfToken)

/**
 * Refresh a Session
 */
suspend fun SuperTokens.refreshSession(
    refreshToken: String,
    enableAntiCsrf: Boolean = false,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().refreshSession(refreshToken, enableAntiCsrf, antiCsrfToken)

/**
 * Regenerate a session
 */
suspend fun SuperTokens.regenerateSession(
    accessToken: String,
    userDataInJWT: Map<String, Any?>? = null,
) = getRecipe<SessionRecipe>().regenerateSession(accessToken, userDataInJWT)

/**
 * Change session data
 */
suspend fun SuperTokens.updateSessionData(
    accessToken: String,
    userDataInDatabase: Map<String, Any?>,
) = getRecipe<SessionRecipe>().updateSessionData(accessToken, userDataInDatabase)

/**
 * Change JWT data for a session
 */
suspend fun SuperTokens.updateJwtData(
    accessToken: String,
    userDataInJWT: Map<String, Any?>,
) = getRecipe<SessionRecipe>().updateJwtData(accessToken, userDataInJWT)