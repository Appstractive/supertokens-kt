package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.emailpassword.updatePassword
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.assertEquals

abstract class BaseTest(
    connectionUrl: String = "http://localhost:3567/"
) {

    internal val superTokens = superTokens(
        connectionURI = connectionUrl,
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
        configure()
    }

    @Before
    fun setup(): Unit = runBlocking {
        val result = runCatching {
            superTokens.emailPasswordSignUp(TEST_USER, TEST_PASSWORD)
        }

        result.onFailure {
            if (it is SuperTokensStatusException && it.status == SuperTokensStatus.EMailAlreadyExistsError) {
                val user = getRecipeUser(TEST_USER)
                assertEquals(
                    SuperTokensStatus.OK,
                    superTokens.updatePassword(user.id, TEST_PASSWORD)
                )
            }
        }
    }

    suspend fun getRecipeUser(email: String, recipeId: String = RECIPE_EMAIL_PASSWORD): User {
        return superTokens.getUsersByEMail(email)
            .first {
                user -> user.loginMethods?.any {
                    method -> method.recipeId == recipeId
                } == true
            }
    }

    abstract fun SuperTokensConfig.configure()

    companion object {
        const val TEST_USER = "test@test.de"
        const val TEST_PASSWORD = "a1234567"
    }

}
