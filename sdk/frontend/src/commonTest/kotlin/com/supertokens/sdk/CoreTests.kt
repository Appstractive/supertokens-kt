package com.supertokens.sdk

import com.supertokens.sdk.recipes.core.checkEmailExists
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.checkPhoneNumberExists
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoreTests {

    private val client = superTokensClient("https://auth.appstractive.com") {
        recipe(EmailPassword)
        recipe(Passwordless)
    }

    @Test
    fun testEmailExists() = runBlocking {
        val exists = client.checkEmailExists("test@test.de")
        assertTrue(exists)
    }

    @Test
    fun testPhoneNumberExists() = runBlocking {
        val exists = client.checkPhoneNumberExists("+491601234567")
        assertFalse(exists)
    }

}