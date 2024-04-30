package com.supertokens.sdk.recipes.session

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_SESSION
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.get
import com.supertokens.sdk.models.CreateSessionData
import com.supertokens.sdk.models.RegenerateSessionData
import com.supertokens.sdk.post
import com.supertokens.sdk.put
import com.supertokens.sdk.recipes.CustomJwtData
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.multifactor.AuthFactor
import com.supertokens.sdk.recipes.session.models.GetSessionData
import com.supertokens.sdk.recipes.session.models.VerifySessionData
import com.supertokens.sdk.recipes.session.requests.CreateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RefreshSessionRequest
import com.supertokens.sdk.recipes.session.requests.RegenerateSessionRequest
import com.supertokens.sdk.recipes.session.requests.RemoveSessionsForUserRequest
import com.supertokens.sdk.recipes.session.requests.RemoveSessionsRequest
import com.supertokens.sdk.recipes.session.requests.UpdateJwtDataRequest
import com.supertokens.sdk.recipes.session.requests.UpdateSessionDataRequest
import com.supertokens.sdk.recipes.session.requests.VerifySessionRequest
import com.supertokens.sdk.recipes.session.responses.CreateSessionResponseDTO
import com.supertokens.sdk.recipes.session.responses.GetSessionResponseDTO
import com.supertokens.sdk.recipes.session.responses.GetSessionsResponseDTO
import com.supertokens.sdk.recipes.session.responses.RegenerateSessionResponseDTO
import com.supertokens.sdk.recipes.session.responses.RemoveSessionsResponseDTO
import com.supertokens.sdk.recipes.session.responses.VerifySessionResponseDTO
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.toData
import io.ktor.client.request.header
import io.ktor.client.request.setBody


class SessionConfig : RecipeConfig {

    // if true, attach tokens to response headers
    var headerBasedSessions: Boolean = true

    // if true, set tokens to cookies
    var cookieBasedSessions: Boolean = true

    var verifySessionInCore: Boolean = false

    var cookieDomain: String? = null

    // the JWT issuer to use
    var issuer: String? = null

    var antiCsrfCheck: Boolean = false

    var useDynamicSigningKey: Boolean = false

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
    val headerBasedSessions by lazy {
        config.headerBasedSessions
    }

    // if true, set tokens to cookies
    val cookieBasedSessions by lazy {
        config.cookieBasedSessions
    }

    val cookieDomain by lazy {
        config.cookieDomain ?: superTokens.appConfig.api.host
    }

    val secureCookies by lazy {
        superTokens.appConfig.api.scheme == "https"
    }

    val cookieSameSite by lazy {
        // will be 'none' if https is used and frontend and api are different hosts, else 'lax'
        if (
            secureCookies && cookieDomain != superTokens.appConfig.api.host
        ) {
            "none"
        } else {
            "lax"
        }
    }

    val enableAntiCsrfCheck = config.antiCsrfCheck

    val verifySessionInCore = config.verifySessionInCore

    val useDynamicSigningKey = config.useDynamicSigningKey

    // attach any additional data to Jwts on session creation and regeneration
    val customJwtData: CustomJwtData? = config.customJwtData

    // the JWT issuer to use
    val issuer by lazy {
        config.issuer ?: superTokens.appConfig.api.host
    }

    suspend fun getJwtData(
        user: User,
        tenantId: String?,
        recipeId: String,
        multiAuthFactor: AuthFactor?,
        accessToken: String?
    ): Map<String, Any?> = buildMap {
        set(Claims.ISSUER, issuer)
        set(Claims.AUDIENCE, superTokens.appConfig.frontends.map { it.host })

        user.email?.let {
            set(Claims.EMAIL, it)
        }
        user.phoneNumber?.let {
            set(Claims.PHONE_NUMBER, it)
        }

        superTokens.recipes.forEach {
            it.getExtraJwtData(
                user = user,
                tenantId = tenantId,
                recipeId = recipeId,
                authFactor = multiAuthFactor,
                accessToken = accessToken,
            ).forEach { entry ->
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
    @Throws(SuperTokensStatusException::class)
    suspend fun createSession(
        userId: String,
        tenantId: String?,
        userDataInJWT: Map<String, Any?> = emptyMap(),
        userDataInDatabase: Map<String, Any?> = emptyMap(),
    ): CreateSessionData {
        val response = superTokens.post(PATH_SESSION, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                CreateSessionRequest(
                    userId = userId,
                    userDataInJWT = userDataInJWT.toJsonElement(),
                    userDataInDatabase = userDataInDatabase.toJsonElement(),
                    enableAntiCsrf = enableAntiCsrfCheck,
                    useDynamicSigningKey = useDynamicSigningKey,
                )
            )
        }

        return response.parse<CreateSessionResponseDTO, CreateSessionData> {
            CreateSessionData(
                session = checkNotNull(it.session).toData(),
                accessToken = checkNotNull(it.accessToken),
                refreshToken = checkNotNull(it.refreshToken),
                antiCsrfToken = it.antiCsrfToken,
            )
        }
    }

    /**
     * Get user and session information for a given session handle
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun getSession(sessionHandle: String): GetSessionData {
        val response = superTokens.get(
            PATH_SESSION,
            tenantId = null,
            queryParams = mapOf(
                "sessionHandle" to sessionHandle,
            ),
        ) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)
        }

        return response.parse<GetSessionResponseDTO, GetSessionData> {
            GetSessionData(
                userId = checkNotNull(it.userId),
                expiry = checkNotNull(it.expiry),
                timeCreated = checkNotNull(it.timeCreated),
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
    @Throws(SuperTokensStatusException::class)
    suspend fun getSessions(userId: String, tenantId: String?): List<String> {
        val response = superTokens.get(
            PATH_SESSIONS,
            tenantId = tenantId,
            queryParams = mapOf(
                "userId" to userId,
                "fetchAcrossAllTenants" to (tenantId == null).toString(),
            ),
        ) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)
        }

        return response.parse<GetSessionsResponseDTO, List<String>> {
            it.sessionHandles
        }
    }

    /**
     * Delete sessions
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun removeSessions(sessionHandles: List<String>, tenantId: String?): List<String> {
        if (sessionHandles.isEmpty()) {
            return emptyList()
        }

        val response = superTokens.post(PATH_SESSION_REMOVE, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                RemoveSessionsRequest(
                    sessionHandles = sessionHandles,
                )
            )
        }

        return response.parse<RemoveSessionsResponseDTO, List<String>> {
            it.sessionHandlesRevoked
        }
    }

    /**
     * Delete sessions for a user
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun removeSessionsForUser(userId: String, tenantId: String?): List<String> {
        val response = superTokens.post(PATH_SESSION_REMOVE, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                RemoveSessionsForUserRequest(
                    userId = userId,
                    revokeAcrossAllTenants = tenantId == null,
                )
            )
        }

        return response.parse<RemoveSessionsResponseDTO, List<String>> {
            it.sessionHandlesRevoked
        }
    }

    /**
     * Verify a Session
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun verifySession(
        accessToken: String,
        doAntiCsrfCheck: Boolean = false,
        checkDatabase: Boolean = false,
        antiCsrfToken: String? = null,
    ): VerifySessionData {

        val response = superTokens.post(PATH_SESSION_VERIFY, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                VerifySessionRequest(
                    accessToken = accessToken,
                    enableAntiCsrf = enableAntiCsrfCheck,
                    doAntiCsrfCheck = doAntiCsrfCheck,
                    checkDatabase = checkDatabase,
                    antiCsrfToken = antiCsrfToken,
                )
            )
        }

        return response.parse<VerifySessionResponseDTO, VerifySessionData> {
            VerifySessionData(
                session = checkNotNull(it.session?.toData()),
                accessToken = it.accessToken,
            )
        }
    }

    /**
     * Refresh a Session
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun refreshSession(
        refreshToken: String,
        antiCsrfToken: String? = null,
    ): CreateSessionData {
        val response = superTokens.post(PATH_SESSION_REFRESH, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                RefreshSessionRequest(
                    refreshToken = refreshToken,
                    enableAntiCsrf = enableAntiCsrfCheck,
                    antiCsrfToken = antiCsrfToken,
                )
            )
        }

        return response.parse<CreateSessionResponseDTO, CreateSessionData> {
            CreateSessionData(
                session = checkNotNull(it.session).toData(),
                accessToken = checkNotNull(it.accessToken),
                refreshToken = checkNotNull(it.refreshToken),
                antiCsrfToken = it.antiCsrfToken,
            )
        }
    }

    /**
     * Regenerate a session
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun regenerateSession(
        accessToken: String,
        userDataInJWT: Map<String, Any?>? = null,
    ): RegenerateSessionData {

        val response = superTokens.post(PATH_SESSION_REGENERATE, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

            setBody(
                RegenerateSessionRequest(
                    accessToken = accessToken,
                    userDataInJWT = userDataInJWT?.toJsonElement(),
                )
            )
        }

        return response.parse<RegenerateSessionResponseDTO, RegenerateSessionData> {
            RegenerateSessionData(
                session = it.session?.toData()
                    ?: throw RuntimeException("RegenerateSession returned OK but no session"),
                accessToken = it.accessToken
                    ?: throw RuntimeException("RegenerateSession returned OK but no accessToken"),
            )
        }
    }

    /**
     * Change session data
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun updateSessionData(
        sessionHandle: String,
        userDataInDatabase: Map<String, Any?>
    ): SuperTokensStatus {
        val response = superTokens.put(PATH_SESSION_DATA, tenantId = null) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

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
    @Throws(SuperTokensStatusException::class)
    suspend fun updateJwtData(
        sessionHandle: String,
        userDataInJWT: Map<String, Any?>,
        tenantId: String?
    ): SuperTokensStatus {
        val response = superTokens.put(PATH_JWT_DATA, tenantId = tenantId) {

            header(HEADER_RECIPE_ID, RECIPE_SESSION)

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
        const val PATH_SESSION = "/recipe/session"
        const val PATH_SESSION_REMOVE = "/recipe/session/remove"
        const val PATH_SESSION_VERIFY = "/recipe/session/verify"
        const val PATH_SESSION_REFRESH = "/recipe/session/refresh"
        const val PATH_SESSION_REGENERATE = "/recipe/session/regenerate"
        const val PATH_SESSION_DATA = "/recipe/session/data"
        const val PATH_JWT_DATA = "/recipe/jwt/data"
        const val PATH_SESSIONS = "/recipe/session/user"
    }

}

val Sessions = object : RecipeBuilder<SessionConfig, SessionRecipe>() {

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
    tenantId: String? = null,
    userDataInJWT: Map<String, Any?> = emptyMap(),
    userDataInDatabase: Map<String, Any?> = emptyMap(),
) = getRecipe<SessionRecipe>().createSession(
    userId = userId,
    tenantId = tenantId,
    userDataInJWT = userDataInJWT,
    userDataInDatabase = userDataInDatabase,
)

/**
 * Get user and session information for a given session handle
 */
suspend fun SuperTokens.getSession(
    sessionHandle: String,
) = getRecipe<SessionRecipe>().getSession(sessionHandle = sessionHandle)

/**
 * Get session handles for a user
 */
suspend fun SuperTokens.getSessions(
    userId: String,
    tenantId: String? = null,
) = getRecipe<SessionRecipe>().getSessions(
    userId = userId,
    tenantId = tenantId,
)

/**
 * Delete a sesion
 */
suspend fun SuperTokens.removeSessions(
    sessionHandles: List<String>,
    tenantId: String? = null,
) = getRecipe<SessionRecipe>().removeSessions(
    sessionHandles = sessionHandles,
    tenantId = tenantId,
)

/**
 * Verify a Session
 */
suspend fun SuperTokens.verifySession(
    accessToken: String,
    doAntiCsrfCheck: Boolean = false,
    checkDatabase: Boolean = false,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().verifySession(
    accessToken = accessToken,
    doAntiCsrfCheck = doAntiCsrfCheck,
    checkDatabase = checkDatabase,
    antiCsrfToken = antiCsrfToken
)

/**
 * Refresh a Session
 */
suspend fun SuperTokens.refreshSession(
    refreshToken: String,
    antiCsrfToken: String? = null,
) = getRecipe<SessionRecipe>().refreshSession(
    refreshToken = refreshToken,
    antiCsrfToken = antiCsrfToken
)

/**
 * Regenerate a session
 */
suspend fun SuperTokens.regenerateSession(
    accessToken: String,
    userDataInJWT: Map<String, Any?>? = null,
) = getRecipe<SessionRecipe>().regenerateSession(
    accessToken = accessToken,
    userDataInJWT = userDataInJWT
)

/**
 * Change session data
 */
suspend fun SuperTokens.updateSessionData(
    sessionHandle: String,
    userDataInDatabase: Map<String, Any?>,
) = getRecipe<SessionRecipe>().updateSessionData(
    sessionHandle = sessionHandle,
    userDataInDatabase = userDataInDatabase
)

/**
 * Change JWT data for a session
 */
suspend fun SuperTokens.updateJwtData(
    sessionHandle: String,
    userDataInJWT: Map<String, Any?>,
    tenantId: String? = null,
) = getRecipe<SessionRecipe>().updateJwtData(
    sessionHandle = sessionHandle,
    userDataInJWT = userDataInJWT,
    tenantId = tenantId,
)