package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.FormField
import com.supertokens.sdk.recipes.emailpassword.createResetPasswordToken
import com.supertokens.sdk.recipes.emailpassword.getUserByEmail
import com.supertokens.sdk.recipes.emailpassword.emailPasswordGetUserById
import com.supertokens.sdk.recipes.emailpassword.resetPasswordWithToken
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.emailpassword.updateEmail
import com.supertokens.sdk.recipes.emailpassword.updatePassword
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmailPasswordRecipeTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
            apiDomain = "localhost",
            websiteDomain = "localhost",
        ),
    ) {
        recipe(EmailPassword)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<EmailPasswordRecipe>()
        assertEquals(2, recipe.formFields.size)
        assertEquals(FormField.FORM_FIELD_EMAIL_ID, recipe.formFields[0].id)
        assertEquals(FormField.FORM_FIELD_PASSWORD_ID, recipe.formFields[1].id)
    }

    @Test
    fun testCreateUser() = runBlocking {
        val response = superTokens.emailPasswordSignUp("test@test.de", "a1234567")

        assertTrue(response.isRight)
    }

    @Test
    fun testSignInUser() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
    }

    @Test
    fun testGetUserById() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val getResponse = superTokens.emailPasswordGetUserById(user.id)

        assertTrue(getResponse.isRight)
    }

    @Test
    fun testGetUserByMail() = runBlocking {
        val response = superTokens.getUserByEmail("test@test.de")

        assertTrue(response.isRight)
    }

    @Test
    fun testPasswordReset() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        val resetTokenResponse = superTokens.createResetPasswordToken(user.id)
        assertTrue(resetTokenResponse.isRight)
        val token = resetTokenResponse.get()

        val resetResponse = superTokens.resetPasswordWithToken(token, "a1234567")
        assertTrue(resetResponse.isRight)
        assertEquals(user.id, resetResponse.get())
    }

    @Test
    fun testChangeEMail() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, "test2@test.de"))
        assertEquals(SuperTokensStatus.OK, superTokens.updateEmail(user.id, "test@test.de"))
    }

    @Test
    fun testChangePassword() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, "1abcdefg"))
        assertEquals(SuperTokensStatus.OK, superTokens.updatePassword(user.id, "a1234567"))
    }

    @Test
    fun testChangePasswordDefaultPolicyError() = runBlocking {
        val response = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        assertTrue(response.isRight)
        val user = response.get()

        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "abcdefgh"))
        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "12345678"))
        assertEquals(SuperTokensStatus.PasswordPolicyViolatedError, superTokens.updatePassword(user.id, "abc123"))
    }

}