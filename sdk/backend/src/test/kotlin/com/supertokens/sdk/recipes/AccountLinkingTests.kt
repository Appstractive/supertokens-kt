package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.accountlinking.AccountLinking
import com.supertokens.sdk.recipes.accountlinking.checkCanCreatePrimaryUser
import com.supertokens.sdk.recipes.accountlinking.checkCanLinkAccounts
import com.supertokens.sdk.recipes.accountlinking.createPrimaryUser
import com.supertokens.sdk.recipes.accountlinking.linkAccounts
import com.supertokens.sdk.recipes.accountlinking.unlinkAccounts
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.consumePasswordlessLinkCode
import com.supertokens.sdk.recipes.passwordless.createPasswordlessEmailCode
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AccountLinkingTests : BaseTest() {
  override fun SuperTokensConfig.configure() {
    recipe(EmailPassword)
    recipe(Passwordless)
    recipe(AccountLinking)
  }

  @Test
  fun testCanCreatePrimaryUser() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val user = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

    val wasAlreadyPrimary =
        superTokens.checkCanCreatePrimaryUser(recipeUserId = user.recipeUserId ?: user.id)
    assertFalse(wasAlreadyPrimary)
  }

  @Test
  fun testCreatePrimaryUser() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val user = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

    val createPrimaryUserResult =
        superTokens.createPrimaryUser(recipeUserId = user.recipeUserId ?: user.id)
    assertFalse(createPrimaryUserResult.wasAlreadyAPrimaryUser)
    assertTrue(createPrimaryUserResult.user.isPrimaryUser == true)
  }

  @Test
  fun testCanLinkAccounts() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val emailPasswordUser = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

    val code = superTokens.createPasswordlessEmailCode(email)
    val passwordlessUser =
        superTokens.consumePasswordlessLinkCode(code.preAuthSessionId, code.linkCode).user

    val createPrimaryUserResult =
        superTokens.createPrimaryUser(
            recipeUserId = passwordlessUser.recipeUserId ?: passwordlessUser.id)

    val canLinksAccountsResult =
        superTokens.checkCanLinkAccounts(
            primaryUserId = createPrimaryUserResult.user.id,
            recipeUserId = emailPasswordUser.recipeUserId ?: emailPasswordUser.id,
        )

    assertFalse(canLinksAccountsResult)
  }

  @Test
  fun testLinkAccounts() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val emailPasswordUser = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

    val code = superTokens.createPasswordlessEmailCode(email)
    val passwordlessUser =
        superTokens.consumePasswordlessLinkCode(code.preAuthSessionId, code.linkCode).user

    val createPrimaryUserResult =
        superTokens.createPrimaryUser(
            recipeUserId = passwordlessUser.recipeUserId ?: passwordlessUser.id)

    val linksAccountsResult =
        superTokens.linkAccounts(
            primaryUserId = createPrimaryUserResult.user.id,
            recipeUserId = emailPasswordUser.recipeUserId ?: emailPasswordUser.id,
        )

    assertFalse(linksAccountsResult.accountsAlreadyLinked)
  }

  @Test
  fun testUnlinkAccounts() = runBlocking {
    val email = "${Instant.now().toEpochMilli()}@test.de"
    val emailPasswordUser = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

    val code = superTokens.createPasswordlessEmailCode(email)
    val passwordlessUser =
        superTokens.consumePasswordlessLinkCode(code.preAuthSessionId, code.linkCode).user

    val createPrimaryUserResult =
        superTokens.createPrimaryUser(
            recipeUserId = passwordlessUser.recipeUserId ?: passwordlessUser.id)

    val linksAccountsResult =
        superTokens.linkAccounts(
            primaryUserId = createPrimaryUserResult.user.id,
            recipeUserId = emailPasswordUser.recipeUserId ?: emailPasswordUser.id,
        )

    assertFalse(linksAccountsResult.accountsAlreadyLinked)

    val unlinkResult =
        superTokens.unlinkAccounts(
            recipeUserId = emailPasswordUser.recipeUserId ?: emailPasswordUser.id,
        )

    assertTrue(unlinkResult.wasLinked)
    assertFalse(unlinkResult.wasRecipeUserDeleted)
  }
}
