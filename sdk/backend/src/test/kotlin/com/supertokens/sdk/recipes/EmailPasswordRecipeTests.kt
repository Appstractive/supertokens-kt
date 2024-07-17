package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.createResetPasswordToken
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.emailpassword.resetPasswordWithToken
import com.supertokens.sdk.recipes.emailpassword.updateEmail
import com.supertokens.sdk.recipes.emailpassword.updatePassword
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EmailPasswordRecipeTests : BaseTest() {

  override fun SuperTokensConfig.configure() {
    recipe(EmailPassword) {
      emailService =
          SmtpEmailService(
              SmtpConfig(
                  host = "localhost",
                  port = 1025,
                  password = "",
                  fromEmail = "test@example.com",
                  fromName = "SuperTokens Test",
              ))
    }
  }

  @Test
  fun testConfig() {
    val recipe = superTokens.getRecipe<EmailPasswordRecipe>()
    assertEquals(2, recipe.formFields.size)
    assertEquals(FORM_FIELD_EMAIL_ID, recipe.formFields[0].id)
    assertEquals(FORM_FIELD_PASSWORD_ID, recipe.formFields[1].id)
  }

  @Test
  fun testCreateUser() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val user = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)
    assertEquals(email, user.email)
  }

  @Test
  fun testSignInUser() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val user = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)
    val signedInUser = superTokens.emailPasswordSignIn(email, TEST_PASSWORD)
    assertEquals(signedInUser.id, user.id)
  }

  @Test
  fun testPasswordReset() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    val token = superTokens.createResetPasswordToken(user.id, TEST_USER)

    val resetResponse = superTokens.resetPasswordWithToken(token, TEST_PASSWORD)
    assertEquals(user.id, resetResponse)
  }

  @Test
  fun testChangeEMail() = runBlocking {
    val user = getRecipeUser(TEST_USER)

    assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, "test2@test.de"))
    assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, TEST_USER))
  }

  @Test
  fun testChangePassword() = runBlocking {
    val user = getRecipeUser(TEST_USER)

    assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, "1abcdefg"))
    assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, TEST_PASSWORD))
  }

  @Test
  fun testChangePasswordDefaultPolicyError() = runBlocking {
    val user = superTokens.getUsersByEMail(TEST_USER).first()

    assertEquals(
        SuperTokensStatus.PasswordPolicyViolatedError,
        superTokens.updatePassword(user.id, "abcdefgh"))
    assertEquals(
        SuperTokensStatus.PasswordPolicyViolatedError,
        superTokens.updatePassword(user.id, "12345678"))
    assertEquals(
        SuperTokensStatus.PasswordPolicyViolatedError,
        superTokens.updatePassword(user.id, "abc123"))
  }
}
