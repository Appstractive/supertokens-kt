package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.deleteUser
import com.supertokens.sdk.core.getUserByEMail
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.core.getUserByPhoneNumber
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
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
        ),
    ) {
        recipe(EmailPassword)
    }

    @Test
    fun testGetUserById() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

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

    @Test
    fun testDeleteUser() = runBlocking {
        val user = superTokens.emailPasswordSignUp("test42@test.de", "a1234567")

        val response = superTokens.deleteUser(user.id)
        assertEquals(SuperTokensStatus.OK, response)
    }

}