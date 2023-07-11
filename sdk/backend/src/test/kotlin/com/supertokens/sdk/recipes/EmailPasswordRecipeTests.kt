package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.createResetPasswordToken
import com.supertokens.sdk.recipes.emailpassword.resetPasswordWithToken
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.emailpassword.updateEmail
import com.supertokens.sdk.recipes.emailpassword.updatePassword
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

@Ignore("Only for DEV purposes")
class EmailPasswordRecipeTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
        recipe(EmailPassword) {
            emailService = SmtpEmailService(
                SmtpConfig(
                    host = "localhost",
                    port = 1025,
                    password = "",
                    fromEmail = "test@example.com",
                    fromName = "SuperTokens Test",
                )
            )
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
        val user = superTokens.emailPasswordSignUp("test@test.de", "a1234567")
        assertEquals("test@test.de", user.email)
    }

    @Test
    fun testSignInUser() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")
        assertEquals("test@test.de", user.email)
    }

    @Test
    fun testPasswordReset() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val token = superTokens.createResetPasswordToken(user.id)

        val resetResponse = superTokens.resetPasswordWithToken(token, "a1234567")
        assertEquals(user.id, resetResponse)
    }

    @Test
    fun testChangeEMail() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, "test2@test.de"))
        assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, "test@test.de"))
    }

    @Test
    fun testChangePassword() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, "1abcdefg"))
        assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, "a1234567"))
    }

    @Test
    fun testChangePasswordDefaultPolicyError() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "abcdefgh"))
        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "12345678"))
        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "abc123"))
    }

}