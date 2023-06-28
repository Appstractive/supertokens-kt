package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.recipes.emailpassword.emailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.session.SessionRecipe
import com.supertokens.sdk.recipes.session.session
import com.supertokens.sdk.recipes.session.createSession
import com.supertokens.sdk.recipes.session.getSession
import com.supertokens.sdk.recipes.session.getSessions
import com.supertokens.sdk.recipes.session.refreshSession
import com.supertokens.sdk.recipes.session.regenerateSession
import com.supertokens.sdk.recipes.session.removeSessions
import com.supertokens.sdk.recipes.session.updateJwtData
import com.supertokens.sdk.recipes.session.updateSessionData
import com.supertokens.sdk.recipes.session.verifySession
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SessionRecipeTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
            apiDomain = "localhost",
            websiteDomain = "localhost",
        ),
    ) {
        emailPassword {

        }
        session {

        }
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<SessionRecipe>()
    }

    @Test
    fun testCreateSession() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val session = superTokens.createSession(userId = user.id, userDataInJWT = jwtData, userDataInDatabase = dbData)
        assertTrue(session.isRight)
    }

    @Test
    fun testGetSession() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(userId = user.id, userDataInJWT = jwtData, userDataInDatabase = dbData)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val getResponse = superTokens.getSession(session.session.handle)
        assertTrue(getResponse.isRight)

        assertEquals(session.session.handle, getResponse.get().sessionHandle)
    }

    @Test
    fun testGetSessions() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val getSessionsResponse = superTokens.getSessions(user.id)
        assertTrue(getSessionsResponse.isRight)
        val sessions = getSessionsResponse.get()

        assertTrue(sessions.contains(session.session.handle))
    }

    @Test
    fun testRemoveSessions() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val getSessionsResponse = superTokens.removeSessions(listOf(session.session.handle))
        assertTrue(getSessionsResponse.isRight)
        val sessions = getSessionsResponse.get()

        assertTrue(sessions.contains(session.session.handle))
    }

    @Test
    fun testVerifySession() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val verifySessionsResponse = superTokens.verifySession(
            session.accessToken.token,
        )
        assertTrue(verifySessionsResponse.isRight)
    }

    @Test
    fun testRefreshSession() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val refreshSessionsResponse = superTokens.refreshSession(
            session.refreshToken.token,
        )
        assertTrue(refreshSessionsResponse.isRight)

        assertEquals(session.session.handle, refreshSessionsResponse.get().session.handle)
    }

    @Test
    fun testRegenerateSession() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val regenerateSessionsResponse = superTokens.regenerateSession(
            session.accessToken.token,
            jwtData,
        )
        assertTrue(regenerateSessionsResponse.isRight)

        assertEquals(session.session.handle, regenerateSessionsResponse.get().session.handle)
        assertNotEquals(null, regenerateSessionsResponse.get().session.userDataInJWT)
    }

    @Test
    fun testUpdateSessionData() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val updateResponse = superTokens.updateSessionData(
            session.session.handle,
            dbData,
        )
        assertEquals(SuperTokensStatus.OK, updateResponse)
    }

    @Test
    fun testUpdateJwtData() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val sessionResponse = superTokens.createSession(user.id)
        assertTrue(sessionResponse.isRight)
        val session = sessionResponse.get()

        val updateResponse = superTokens.updateJwtData(
            session.session.handle,
            jwtData,
        )
        assertEquals(SuperTokensStatus.OK, updateResponse)
    }

    private enum class TestEnum {
        VALUE1,
        VALUE2,
        VALUE3,
    }

    @Serializable
    private data class TestClass(
        val bool: Boolean,
        val number: Double,
        val string: String,
        val enum: TestEnum,
    )

    companion object {
        private val jsonEncoder = Json { encodeDefaults = true }

        val jwtData: Map<String, Any?> = mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to listOf<Any?>(
                null,
                false,
                789.123,
                "World",
                TestEnum.VALUE2,
            ),
            "object" to mapOf<String, Any?>(
                "null" to null,
                "bool" to true,
                "number" to 42,
                "string" to "Hello",
                "enum" to TestEnum.VALUE3,
            ),
            "complex" to jsonEncoder.encodeToJsonElement(
                TestClass(
                    bool = true,
                    number = 169.270,
                    string = "!",
                    enum = TestEnum.VALUE2,
                )
            ),
        )

        val dbData: Map<String, Any?> = mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to listOf<Any?>(
                null,
                false,
                789.123,
                "World",
                TestEnum.VALUE2,
            ),
            "object" to mapOf<String, Any?>(
                "null" to null,
                "bool" to true,
                "number" to 42,
                "string" to "Hello",
                "enum" to TestEnum.VALUE3,
            ),
            "complex" to jsonEncoder.encodeToJsonElement(
                TestClass(
                    bool = true,
                    number = 169.270,
                    string = "!",
                    enum = TestEnum.VALUE2,
                )
            ),
        )
    }

}