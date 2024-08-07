package com.supertokens.sdk.recipes

import com.auth0.jwt.JWT
import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.ThirdPartyAuth
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ThirdPartyAuthRecipeTests : BaseTest() {

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
        privateKey = ThirdPartyAuthRecipeTests.privateKey
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

    val github = recipe.getProviderById(ThirdPartyAuth.GITHUB)
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

    val apple = recipe.getProviderById(ThirdPartyAuth.APPLE) as? AppleProvider
    assertNotNull(apple)
    assertEquals(
        "https://appleid.apple.com/auth/authorize?scope=email&response_mode=form_post&response_type=code&client_id=123456&redirect_uri=app%3A%2F%2Fredirect",
        apple.getAuthorizationEndpoint("app://redirect").fullUrl,
    )

    val clientSecret = apple.clientSecret
    val jwt = JWT.decode(clientSecret)
    assertEquals("https://appleid.apple.com", jwt.audience.first())
    assertEquals("teamid", jwt.issuer)
    assertEquals("123456", jwt.subject)
  }

  @Test
  fun testFacebook() {
    val recipe = superTokens.getRecipe<ThirdPartyRecipe>()

    val facebook = recipe.getProviderById(ThirdPartyAuth.FACEBOOK)
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

    val bitbucket = recipe.getProviderById(ThirdPartyAuth.BITBUCKET)
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

    val gitlab = recipe.getProviderById(ThirdPartyAuth.GITLAB)
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

    val google = recipe.getProviderById(ThirdPartyAuth.GOOGLE)
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

    const val privateKey =
        "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgMKZfBMuF5CyRyXAiQaYew+j3U+bQ5oRtqb/3SujLlZGgCgYIKoZIzj0DAQehRANCAAQoYTJsktscfGFwAm40TH2648sGmHS5qvti8tvRcXF6v5Gu0fecAPooXDFqn63ZfStZjQm/3cMsPWwDKo3QryFm"
  }
}
