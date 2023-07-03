package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.recipes.emailverification.EmailVerificationRecipe
import com.supertokens.sdk.recipes.emailverification.createEmailVerificationToken
import com.supertokens.sdk.recipes.emailverification.removeAllVerificationTokens
import com.supertokens.sdk.recipes.emailverification.setUnverified
import com.supertokens.sdk.recipes.emailverification.verifyEmail
import com.supertokens.sdk.recipes.emailverification.verifyToken
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore("Only for DEV purposes")
class EmailVerificationTest {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
            apiDomain = "localhost",
            websiteDomain = "localhost",
        ),
    ) {
        recipe(EmailPassword)
        recipe(EmailVerification)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<EmailVerificationRecipe>()
    }

    @Test
    fun testCreateVerificationToken() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val token = superTokens.createEmailVerificationToken(user.id, "test@test.de")
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun testRemoveAllVerificationTokens() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val status = superTokens.removeAllVerificationTokens(user.id, "test@test.de")
        assertEquals(SuperTokensStatus.OK, status)
    }

    @Test
    fun testVerifyToken() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val token = superTokens.createEmailVerificationToken(user.id, "test@test.de")
        val response = superTokens.verifyToken(token)

        assertEquals("test@test.de", response.email)
        assertEquals(user.id, response.userId)
    }

    @Test
    fun testVerifyEmail() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val token = superTokens.createEmailVerificationToken(user.id, "test@test.de")
        superTokens.verifyToken(token)

        val isValid = superTokens.verifyEmail(user.id, "test@test.de")
        assertEquals(true, isValid)
    }

    @Test
    fun testSetUnverified() = runBlocking {
        val user = superTokens.emailPasswordSignIn("test@test.de", "a1234567")

        val status = superTokens.setUnverified(user.id, "test@test.de")
        assertEquals(SuperTokensStatus.OK, status)
    }

}