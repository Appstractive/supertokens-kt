package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.getUsersByEmail
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.bitbucket.Bitbucket
import com.supertokens.sdk.recipes.thirdparty.providers.facebook.Facebook
import com.supertokens.sdk.recipes.thirdparty.providers.github.Github
import com.supertokens.sdk.recipes.thirdparty.providers.github.GithubProvider
import com.supertokens.sdk.recipes.thirdparty.thirdPartySignInUp
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        recipe(ThirdParty) {

            provider(Github) {
                clientId = "123456"
                clientSecret = "abcdef"
            }

            provider(Facebook) {
                clientId = "123456"
                clientSecret = "abcdef"
            }

            provider(Bitbucket) {
                clientId = "123456"
                clientSecret = "abcdef"
            }
        }
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val github = recipe.getProvider(GithubProvider.ID)
        assertNotNull(github)
        assertEquals(
            "https://github.com/login/oauth/access_token?client_id=123456&client_secret=abcdef&code=auth&redirect_uri=redirect://somewhere",
            github.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://github.com/login/oauth/authorize?scope=read:user user:email&client_id=123456",
            github.getAuthorizationEndpoint().fullUrl,
        )
    }

    @Test
    fun testSignInUp() = runBlocking {
        val response = superTokens.thirdPartySignInUp("google", "12345678", "test@test.de")

        val thirdParty = assertNotNull(response.user.thirdParty)
    }

    @Test
    fun testGetUsersByMail() = runBlocking {
        val response = superTokens.getUsersByEmail("test@test.de")

        assertTrue(response.isNotEmpty())
    }

}