package com.supertokens.sdk

import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.recipes.sessions.Session
import com.supertokens.sdk.recipes.sessions.repositories.TokensRepositoryMemory
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.getThirdPartyAuthorizationUrl
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.Apple
import com.supertokens.sdk.recipes.thirdparty.providers.Bitbucket
import com.supertokens.sdk.recipes.thirdparty.providers.Facebook
import com.supertokens.sdk.recipes.thirdparty.providers.GitHub
import com.supertokens.sdk.recipes.thirdparty.providers.GitLab
import com.supertokens.sdk.recipes.thirdparty.providers.Google
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

@Ignore()
class ThirdPartySignInTests {

  private val client =
      superTokensClient("https://auth.appstractive.cloud") {
        recipe(ThirdParty) {
          provider(Apple) { redirectUri = "localhost" }
          provider(Bitbucket) { redirectUri = "localhost" }
          provider(Facebook) { redirectUri = "localhost" }
          provider(GitHub) { redirectUri = "localhost" }
          provider(GitLab) { redirectUri = "localhost" }
          provider(Google) { redirectUri = "localhost" }
        }
        recipe(Session) { tokensRepository = TokensRepositoryMemory() }
      }

  @Test
  fun testAppleAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(Apple.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testAppleTokenSignIn() = runBlocking {
    val response =
        client.signInWith(Apple.Tokens) {
          accessToken = "123456"
          idToken = "123456"
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testAppleAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(Apple.id)
    assertTrue(authUrl.isNotEmpty())
  }

  @Test
  fun testBitbucketAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(Bitbucket.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testBitbucketTokenSignIn() = runBlocking {
    val response = client.signInWith(Bitbucket.Tokens) { accessToken = "123456" }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testBitbucketAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(Bitbucket.id)
    assertTrue(authUrl.isNotEmpty())
  }

  @Test
  fun testFacebookAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(Facebook.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testFacebookTokenSignIn() = runBlocking {
    val response = client.signInWith(Facebook.Tokens) { accessToken = "123456" }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testFacebookAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(Facebook.id)
    assertTrue(authUrl.isNotEmpty())
  }

  @Test
  fun testGitHubAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(GitHub.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGitHubTokenSignIn() = runBlocking {
    val response = client.signInWith(GitHub.Tokens) { accessToken = "123456" }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGitHubAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(GitHub.id)
    assertTrue(authUrl.isNotEmpty())
  }

  @Test
  fun testGitLabAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(GitLab.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGitLabTokenSignIn() = runBlocking {
    val response = client.signInWith(GitLab.Tokens) { accessToken = "123456" }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGitLabAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(GitLab.id)
    assertTrue(authUrl.isNotEmpty())
  }

  @Test
  fun testGoogleAuthCodeSignIn() = runBlocking {
    val response =
        client.signInWith(Google.AuthCode) {
          redirectURIQueryParams = mapOf("code" to "123456", "state" to "signup")
        }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGoogleTokenSignIn() = runBlocking {
    val response = client.signInWith(Google.Tokens) { accessToken = "123456" }

    assertEquals("test@test.de", response.user.email)
  }

  @Test
  fun testGoogleAuthUrl() = runBlocking {
    val authUrl = client.getThirdPartyAuthorizationUrl(Google.id)
    assertTrue(authUrl.isNotEmpty())
  }
}
