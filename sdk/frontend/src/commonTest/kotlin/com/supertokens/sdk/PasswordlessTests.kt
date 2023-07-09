package com.supertokens.sdk

import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.repositories.tokens.TokensRepositoryMemory
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PasswordlessTests {

    private val client = superTokensClient("https://auth.appstractive.com") {
        tokensRepository = TokensRepositoryMemory()
    }

    @Test
    fun testSignUp() = runBlocking {
        val data = client.signUpWith(Passwordless) {
            email = "test@test.de"
        }

        assertTrue(data.deviceId.isNotEmpty())
    }

    @Test
    fun testSignUpError() = runBlocking {
        val response = runCatching {
            client.signUpWith(Passwordless) {
            }
        }

        assertTrue(response.isFailure)
        val exception = assertIs<SuperTokensStatusException>(response.exceptionOrNull())
        assertEquals(SuperTokensStatus.FormFieldError, exception.status)
    }

    @Test
    fun testSignInLinkCode() = runBlocking {
        val data = client.signInWith(Passwordless) {
            preAuthSessionId = "test@test.de"
            linkCode = "12345"
        }

        assertTrue(data.user.id.isNotEmpty())
    }

    @Test
    fun testSignInUserInputCode() = runBlocking {
        val data = client.signInWith(Passwordless) {
            preAuthSessionId = "test@test.de"
            deviceId = "12345"
            userInputCode = "12345"
        }

        assertTrue(data.user.id.isNotEmpty())
    }

    @Test
    fun testSignInError() = runBlocking {
        val response = runCatching {
            client.signInWith(Passwordless) {
            }
        }

        assertTrue(response.isFailure)
        val exception = assertIs<SuperTokensStatusException>(response.exceptionOrNull())
        assertEquals(SuperTokensStatus.FormFieldError, exception.status)
    }

}