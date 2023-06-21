package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertTrue

class EmailPasswordRecipeTests {

    @Test
    fun testConfig() {

        val superTokens = superTokens(
            connectionURI = "localhost",
            appConfig = AppConfig(
                name = "TestApp",
                apiDomain = "localhost",
                websiteDomain = "localhost",
            ),
        ) {
            emailPassword {

            }
        }

    }

    @Test
    fun testCreateUser() = runBlocking {
        val superTokens = superTokens(
            connectionURI = "https://try.supertokens.com/",
            appConfig = AppConfig(
                name = "TestApp",
                apiDomain = "localhost",
                websiteDomain = "localhost",
            ),
        ) {
            emailPassword {

            }
        }

        val response = superTokens.emailPasswordSignUp("test@test.de", "a1234567")

        assertTrue(response.isRight)
    }

}