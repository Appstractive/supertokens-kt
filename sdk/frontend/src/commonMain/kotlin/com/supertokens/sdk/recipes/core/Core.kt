package com.supertokens.sdk.recipes.core

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ExistsResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.path

suspend fun SuperTokensClient.checkEmailExists(email: String, recipeId: String = RECIPE_EMAIL_PASSWORD): Boolean {
    return when(recipeId) {
        RECIPE_EMAIL_PASSWORD -> getRecipe<EmailPasswordRecipe>().checkEmailExists(email = email)
        RECIPE_PASSWORDLESS -> getRecipe<PasswordlessRecipe>().checkEmailExists(email = email)
        else -> throw IllegalArgumentException("Only EmailPassword and Passwordless recipes are allowed")
    }
}
