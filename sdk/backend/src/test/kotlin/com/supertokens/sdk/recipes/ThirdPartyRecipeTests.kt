package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.apple.Apple
import com.supertokens.sdk.recipes.thirdparty.providers.apple.AppleProvider
import com.supertokens.sdk.recipes.thirdparty.providers.bitbucket.Bitbucket
import com.supertokens.sdk.recipes.thirdparty.providers.facebook.Facebook
import com.supertokens.sdk.recipes.thirdparty.providers.github.Github
import com.supertokens.sdk.recipes.thirdparty.providers.gitlab.GitLab
import com.supertokens.sdk.recipes.thirdparty.providers.google.Google
import com.supertokens.sdk.recipes.thirdparty.thirdPartySignInUp
import io.fusionauth.jwt.Verifier
import io.fusionauth.jwt.domain.Algorithm
import io.fusionauth.jwt.domain.JWT
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ThirdPartyRecipeTests : BaseTest() {

    override fun SuperTokensConfig.configure() {
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

            provider(GitLab) {
                clientId = "123456"
                clientSecret = "abcdef"
            }

            provider(Google) {
                clientId = "123456"
                clientSecret = "abcdef"
            }

            provider(Apple) {
                clientId = "123456"
                keyId = "keyid"
                privateKey = ThirdPartyRecipeTests.privateKey
                teamId = "teamid"
            }
        }
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()
    }

    @Test
    fun testGithub() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val github = recipe.getProviderById(ThirdPartyProvider.GITHUB)
        assertNotNull(github)
        assertEquals(
            "https://github.com/login/oauth/access_token?client_id=123456&client_secret=abcdef&code=auth&redirect_uri=redirect%3A%2F%2Fsomewhere",
            github.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://github.com/login/oauth/authorize?scope=read%3Auser+user%3Aemail&client_id=123456&redirect_uri=app%3A%2F%2Fredirect",
            github.getAuthorizationEndpoint("app://redirect").fullUrl,
        )
    }

    @Test
    fun testApple() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val apple = recipe.getProviderById(ThirdPartyProvider.APPLE) as? AppleProvider
        assertNotNull(apple)
        assertEquals(
            "https://appleid.apple.com/auth/authorize?scope=email&client_id=123456&redirect_uri=app%3A%2F%2Fredirect&response_mode=form_post&response_type=code",
            apple.getAuthorizationEndpoint("app://redirect").fullUrl,
        )

        val clientSecret = apple.clientSecret
        val jwt = JWT.getDecoder().decode(clientSecret, object : Verifier {
            override fun canVerify(algorithm: Algorithm?) = true

            override fun verify(
                algorithm: Algorithm?,
                message: ByteArray?,
                signature: ByteArray?
            ) {
            }
        })
        assertEquals("https://appleid.apple.com", jwt.audience)
        assertEquals("teamid", jwt.issuer)
        assertEquals("123456", jwt.subject)
    }

    @Test
    fun testFacebook() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val facebook = recipe.getProviderById(ThirdPartyProvider.FACEBOOK)
        assertNotNull(facebook)
        assertEquals(
            "https://graph.facebook.com/v9.0/oauth/access_token?client_id=123456&client_secret=abcdef&code=auth&redirect_uri=redirect%3A%2F%2Fsomewhere",
            facebook.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://www.facebook.com/v9.0/dialog/oauth?scope=email&client_id=123456&redirect_uri=app%3A%2F%2Fredirect",
            facebook.getAuthorizationEndpoint("app://redirect").fullUrl,
        )
    }

    @Test
    fun testBitbucket() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val bitbucket = recipe.getProviderById(ThirdPartyProvider.BITBUCKET)
        assertNotNull(bitbucket)
        assertEquals(
            "https://bitbucket.org/site/oauth2/access_token?client_id=123456&client_secret=abcdef&grant_type=authorization_code&code=auth&redirect_uri=redirect%3A%2F%2Fsomewhere",
            bitbucket.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://bitbucket.org/site/oauth2/authorize?scope=account+email&client_id=123456&redirect_uri=app%3A%2F%2Fredirect&access_type=offline&response_type=code",
            bitbucket.getAuthorizationEndpoint("app://redirect").fullUrl,
        )
    }

    @Test
    fun testGitlab() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val gitlab = recipe.getProviderById(ThirdPartyProvider.GITLAB)
        assertNotNull(gitlab)
        assertEquals(
            "https://gitlab.com/oauth/token?client_id=123456&client_secret=abcdef&grant_type=authorization_code&code=auth&redirect_uri=redirect%3A%2F%2Fsomewhere",
            gitlab.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://gitlab.com/oauth/authorize?scope=read_user&client_id=123456&redirect_uri=app%3A%2F%2Fredirect&response_type=code",
            gitlab.getAuthorizationEndpoint("app://redirect").fullUrl,
        )
    }

    @Test
    fun testGoogle() {
        val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

        val google = recipe.getProviderById(ThirdPartyProvider.GOOGLE)
        assertNotNull(google)
        assertEquals(
            "https://oauth2.googleapis.com/token?client_id=123456&client_secret=abcdef&grant_type=authorization_code&code=auth&redirect_uri=redirect%3A%2F%2Fsomewhere",
            google.getAccessTokenEndpoint("auth", "redirect://somewhere").fullUrl,
        )
        assertEquals(
            "https://accounts.google.com/o/oauth2/v2/auth?scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email&client_id=123456&redirect_uri=app%3A%2F%2Fredirect&access_type=offline&response_type=code&include_granted_scopes=true",
            google.getAuthorizationEndpoint("app://redirect").fullUrl,
        )
    }

    @Test
    fun testSignInUp() = runBlocking {
        val response = superTokens.thirdPartySignInUp("google", "12345678", TEST_USER, true)

        val thirdParty = assertNotNull(response.user.thirdParty)
    }

    companion object {

        const val privateKey = "-----BEGIN EC PRIVATE KEY-----\n" +
                "MHcCAQEEIA15hjyZS/pWzMgI4SOlwKbbG4/c+3vQcFCfQaRhoFbzoAoGCCqGSM49\n" +
                "AwEHoUQDQgAEkrYPIhuxDLQg8QKQnnto8JUFb13yWpY+venFhEzjhBwMgFl3oueT\n" +
                "oQJf/l9sIYMIXc6gVnMg/lGEWv0ZANcYqg==\n" +
                "-----END EC PRIVATE KEY-----"
    }

}