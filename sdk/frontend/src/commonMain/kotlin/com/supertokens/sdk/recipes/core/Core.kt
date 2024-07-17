package com.supertokens.sdk.recipes.core

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.client.request.get

suspend fun SuperTokensClient.checkEmailExists(
    email: String,
    recipeId: String = RECIPE_EMAIL_PASSWORD
): Boolean {
  return when (recipeId) {
    RECIPE_EMAIL_PASSWORD -> getRecipe<EmailPasswordRecipe>().checkEmailExists(email = email)
    RECIPE_PASSWORDLESS -> getRecipe<PasswordlessRecipe>().checkEmailExists(email = email)
    else ->
        throw IllegalArgumentException("Only EmailPassword and Passwordless recipes are allowed")
  }
}
