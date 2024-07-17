package com.supertokens.sdk

import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.handlers.FormFieldException
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.sessions.Session
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepositoryMemory
import com.supertokens.sdk.recipes.sessions.repositories.getAccessToken
import com.supertokens.sdk.recipes.sessions.repositories.getRefreshToken
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

@Ignore()
class EmailPasswordTests {

  private val client =
      superTokensClient("https://auth.appstractive.cloud") {
        recipe(EmailPassword)
        recipe(Session) { tokensRepository = TokensRepositoryMemory() }
      }

  @Test
  fun testSignUp() = runBlocking {
    val user =
        client.signUpWith(EmailPassword) {
          email = "test@test.de"
          password = "a1234567"
        }

    assertEquals("test@test.de", user.email)
  }

  @Test
  fun testSignUpPasswordError() = runBlocking {
    val response =
        kotlin.runCatching {
          client.signUpWith(EmailPassword) {
            email = "test@test.de"
            password = "a12"
          }
        }

    assertTrue(response.isFailure)
    val error = assertIs<FormFieldException>(response.exceptionOrNull())
    assertTrue(error.errors.size == 1)
    assertEquals(error.errors[0].id, FORM_FIELD_PASSWORD_ID)
  }

  @Test
  fun testSignUpEmailError() = runBlocking {
    val response =
        kotlin.runCatching {
          client.signUpWith(EmailPassword) {
            email = "test"
            password = "a1234567"
          }
        }

    assertTrue(response.isFailure)
    val error = assertIs<FormFieldException>(response.exceptionOrNull())
    assertTrue(error.errors.size == 1)
    assertEquals(error.errors[0].id, FORM_FIELD_EMAIL_ID)
  }

  @Test
  fun testSignIn() = runBlocking {
    val user =
        client.signInWith(EmailPassword) {
          email = "test@test.de"
          password = "a1234567"
        }

    assertEquals("test@test.de", user.email)

    val accessToken = assertNotNull(client.getAccessToken())
    val refreshToken = assertNotNull(client.getRefreshToken())
  }
}
