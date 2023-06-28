package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignIn
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.getUsersByEmail
import com.supertokens.sdk.recipes.thirdparty.thirdParty
import com.supertokens.sdk.recipes.thirdparty.thirdPartySignInUp
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class ThirdPartyRecipeTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
            apiDomain = "localhost",
            websiteDomain = "localhost",
        ),
    ) {
        thirdParty {

        }
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()
    }

    @Test
    fun testSignInUp() = runBlocking {
        val response = superTokens.thirdPartySignInUp("google", "12345678", "test@test.de")

        assertTrue(response.isRight)
        val user = response.get()
    }

    @Test
    fun testGetUsersByMail() = runBlocking {
        val response = superTokens.getUsersByEmail("test@test.de")

        assertTrue(response.isRight)
        val users = response.get()
    }

}