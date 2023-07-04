package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.core.getUserByEMail
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.core.getUserByPhoneNumber
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.consumePasswordlessUserInputCode
import com.supertokens.sdk.recipes.passwordless.createPasswordlessPhoneNumberCode
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore("Only for DEV purposes")
class CoreTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
            apiDomain = "localhost",
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
        recipe(Passwordless)
    }

    @Test
    fun testGetUserById() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val getResponse = superTokens.getUserById(user.id)

        assertEquals(user.id, getResponse.id)
    }

    @Test
    fun testGetUserByMail() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        assertEquals("test@test.de", user.email)
    }

    @Test
    fun testGetUserByPhoneNumber() = runBlocking {
        val code = superTokens.createPasswordlessPhoneNumberCode("+491601234567")
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.consumePasswordlessUserInputCode(code.preAuthSessionId, code.deviceId, code.userInputCode)
        val user = superTokens.getUserByPhoneNumber("+491601234567")

        assertEquals("+491601234567", user.phoneNumber)
    }

}