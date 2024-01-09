package com.supertokens.sdk

import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.recipes.thirdparty.getThirdPartyAuthorizationUrl
import com.supertokens.sdk.recipes.thirdparty.providers.Apple
import com.supertokens.sdk.recipes.thirdparty.providers.Bitbucket
import com.supertokens.sdk.recipes.thirdparty.providers.Facebook
import com.supertokens.sdk.recipes.thirdparty.providers.GitHub
import com.supertokens.sdk.recipes.thirdparty.providers.GitLab
import com.supertokens.sdk.recipes.thirdparty.providers.Google
import com.supertokens.sdk.repositories.tokens.TokensRepositoryMemory
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore()
class ThirdPartyTests {

    private val client = superTokensClient("https://auth.appstractive.com") {
        tokensRepository = TokensRepositoryMemory()
    }

    @Test
    fun testAppleAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(Apple.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testAppleTokenSignIn() = runBlocking {
        val response = client.signInWith(Apple.Tokens) {
            accessToken = "123456"
            idToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testAppleAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(Apple)
        assertTrue(authUrl.isNotEmpty())
    }

    @Test
    fun testBitbucketAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(Bitbucket.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testBitbucketTokenSignIn() = runBlocking {
        val response = client.signInWith(Bitbucket.Tokens) {
            accessToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testBitbucketAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(Bitbucket)
        assertTrue(authUrl.isNotEmpty())
    }

    @Test
    fun testFacebookAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(Facebook.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testFacebookTokenSignIn() = runBlocking {
        val response = client.signInWith(Facebook.Tokens) {
            accessToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testFacebookAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(Facebook)
        assertTrue(authUrl.isNotEmpty())
    }

    @Test
    fun testGitHubAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(GitHub.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGitHubTokenSignIn() = runBlocking {
        val response = client.signInWith(GitHub.Tokens) {
            accessToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGitHubAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(GitHub)
        assertTrue(authUrl.isNotEmpty())
    }

    @Test
    fun testGitLabAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(GitLab.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGitLabTokenSignIn() = runBlocking {
        val response = client.signInWith(GitLab.Tokens) {
            accessToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGitLabAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(GitLab)
        assertTrue(authUrl.isNotEmpty())
    }

    @Test
    fun testGoogleAuthCodeSignIn() = runBlocking {
        val response = client.signInWith(Google.AuthCode) {
            pkceCodeVerifier = "wsgewhekjdrtkjt"
            redirectURI = "https://auth.appstractive.com/callback"
            redirectURIQueryParams = mapOf(
                "code" to "123456",
                "state" to "signup"
            )
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGoogleTokenSignIn() = runBlocking {
        val response = client.signInWith(Google.Tokens) {
            accessToken = "123456"
        }

        assertEquals("test@test.de", response.user.email)
    }

    @Test
    fun testGoogleAuthUrl() = runBlocking {
        val authUrl = client.getThirdPartyAuthorizationUrl(Google)
        assertTrue(authUrl.isNotEmpty())
    }

}