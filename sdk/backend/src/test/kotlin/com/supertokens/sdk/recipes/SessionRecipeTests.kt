package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.session.SessionRecipe
import com.supertokens.sdk.recipes.session.Sessions
import com.supertokens.sdk.recipes.session.createSession
import com.supertokens.sdk.recipes.session.getSession
import com.supertokens.sdk.recipes.session.getSessions
import com.supertokens.sdk.recipes.session.refreshSession
import com.supertokens.sdk.recipes.session.regenerateSession
import com.supertokens.sdk.recipes.session.removeSessions
import com.supertokens.sdk.recipes.session.updateJwtData
import com.supertokens.sdk.recipes.session.updateSessionData
import com.supertokens.sdk.recipes.session.verifySession
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class SessionRecipeTests : BaseTest() {

  override fun SuperTokensConfig.configure() {
    recipe(EmailPassword)
    recipe(Sessions)
  }

  @Test
  fun testConfig() {
    val recipe = superTokens.getRecipe<SessionRecipe>()
  }

  @Test
  fun testCreateSession() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session =
        superTokens.createSession(
            userId = user.id, userDataInJWT = jwtData, userDataInDatabase = dbData)
    assertEquals(user.id, session.session.userId)
  }

  @Test
  fun testGetSession() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session =
        superTokens.createSession(
            userId = user.id, userDataInJWT = jwtData, userDataInDatabase = dbData)

    val getResponse = superTokens.getSession(session.session.handle)
    assertEquals(session.session.handle, getResponse.sessionHandle)
  }

  @Test
  fun testGetSessions() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val sessions = superTokens.getSessions(user.id)

    assertTrue(sessions.contains(session.session.handle))
  }

  @Test
  fun testRemoveSessions() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val sessions = superTokens.removeSessions(listOf(session.session.handle))

    assertTrue(sessions.contains(session.session.handle))
  }

  @Test
  fun testVerifySession() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val verifySessionsResponse =
        superTokens.verifySession(
            session.accessToken.token,
        )
    assertEquals(session.session.handle, verifySessionsResponse.session.handle)
  }

  @Test
  fun testRefreshSession() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val refreshSessionsResponse =
        superTokens.refreshSession(
            session.refreshToken.token,
        )

    assertEquals(session.session.handle, refreshSessionsResponse.session.handle)
  }

  @Test
  fun testRegenerateSession() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val regenerateSessionsResponse =
        superTokens.regenerateSession(
            session.accessToken.token,
            jwtData,
        )

    assertNotNull(regenerateSessionsResponse.session.userDataInJWT)
    assertEquals(session.session.handle, regenerateSessionsResponse.session.handle)
  }

  @Test
  fun testUpdateSessionData() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val updateResponse =
        superTokens.updateSessionData(
            session.session.handle,
            dbData,
        )
    assertEquals(SuperTokensStatus.OK, updateResponse)
  }

  @Test
  fun testUpdateJwtData() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val session = superTokens.createSession(user.id)

    val updateResponse =
        superTokens.updateJwtData(
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

    val jwtData: Map<String, Any?> =
        mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to
                listOf<Any?>(
                    null,
                    false,
                    789.123,
                    "World",
                    TestEnum.VALUE2,
                ),
            "object" to
                mapOf<String, Any?>(
                    "null" to null,
                    "bool" to true,
                    "number" to 42,
                    "string" to "Hello",
                    "enum" to TestEnum.VALUE3,
                ),
            "complex" to
                jsonEncoder.encodeToJsonElement(
                    TestClass(
                        bool = true,
                        number = 169.270,
                        string = "!",
                        enum = TestEnum.VALUE2,
                    )),
        )

    val dbData: Map<String, Any?> =
        mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to
                listOf<Any?>(
                    null,
                    false,
                    789.123,
                    "World",
                    TestEnum.VALUE2,
                ),
            "object" to
                mapOf<String, Any?>(
                    "null" to null,
                    "bool" to true,
                    "number" to 42,
                    "string" to "Hello",
                    "enum" to TestEnum.VALUE3,
                ),
            "complex" to
                jsonEncoder.encodeToJsonElement(
                    TestClass(
                        bool = true,
                        number = 169.270,
                        string = "!",
                        enum = TestEnum.VALUE2,
                    )),
        )
  }
}
