package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.recipes.emailverification.EmailVerificationRecipe
import com.supertokens.sdk.recipes.emailverification.checkEmailVerified
import com.supertokens.sdk.recipes.emailverification.createEmailVerificationToken
import com.supertokens.sdk.recipes.emailverification.removeAllVerificationTokens
import com.supertokens.sdk.recipes.emailverification.setUnverified
import com.supertokens.sdk.recipes.emailverification.verifyToken
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EmailVerificationTest : BaseTest() {

  override fun SuperTokensConfig.configure() {
    recipe(EmailPassword)
    recipe(EmailVerification)
  }

  @Test
  fun testConfig() {
    val recipe = superTokens.getRecipe<EmailVerificationRecipe>()
  }

  @Test
  fun testCreateVerificationToken() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()
    superTokens.setUnverified(user.id, TEST_USER)

    val token = superTokens.createEmailVerificationToken(user.id, TEST_USER)
    assertTrue(token.isNotEmpty())
  }

  @Test
  fun testRemoveAllVerificationTokens() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val status = superTokens.removeAllVerificationTokens(user.id, TEST_USER)
    assertEquals(SuperTokensStatus.OK, status)
  }

  @Test
  fun testVerifyToken() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()
    superTokens.setUnverified(user.id, TEST_USER)

    val token = superTokens.createEmailVerificationToken(user.id, TEST_USER)
    val response = superTokens.verifyToken(token)

    assertEquals(TEST_USER, response.email)
    assertEquals(user.id, response.userId)
  }

  @Test
  fun testVerifyEmail() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()
    superTokens.setUnverified(user.id, TEST_USER)

    val token = superTokens.createEmailVerificationToken(user.id, TEST_USER)
    superTokens.verifyToken(token)

    val isValid = superTokens.checkEmailVerified(user.id, TEST_USER)
    assertEquals(true, isValid)
  }

  @Test
  fun testSetUnverified() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val status = superTokens.setUnverified(user.id, TEST_USER)
    assertEquals(SuperTokensStatus.OK, status)
  }
}
