package com.supertokens.sdk

import com.supertokens.sdk.recipes.core.checkEmailExists
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.checkPhoneNumberExists
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class CoreTests {

  private val client =
      superTokensClient("https://auth.appstractive.cloud") {
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
