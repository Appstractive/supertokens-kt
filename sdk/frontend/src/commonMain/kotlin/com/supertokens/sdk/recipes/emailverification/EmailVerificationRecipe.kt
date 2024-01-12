package com.supertokens.sdk.recipes.emailverification

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.emailverification.usecases.CheckEmailVerifiedUseCase
import com.supertokens.sdk.recipes.emailverification.usecases.SendEmailVerificationUseCase
import com.supertokens.sdk.recipes.emailverification.usecases.VerifyEmailUseCase

class EmailVerificationConfig: RecipeConfig

class EmailVerificationRecipe(
    private val superTokens: SuperTokensClient,
    private val config: EmailVerificationConfig,
) : Recipe<EmailVerificationConfig> {

    private val sendEmailVerificationUseCase by lazy {
        SendEmailVerificationUseCase(
            client = superTokens.apiClient,
        )
    }

    private val verifyEmailUseCase by lazy {
        VerifyEmailUseCase(
            client = superTokens.apiClient,
        )
    }

    private val checkEmailVerifiedUseCase by lazy {
        CheckEmailVerifiedUseCase(
            client = superTokens.apiClient,
        )
    }

    suspend fun sendVerificationEmail() = sendEmailVerificationUseCase.sendVerificationEmail()

    suspend fun verifyEmail(token: String) = verifyEmailUseCase.verifyEmail(token)

    suspend fun checkEmailVerified() = checkEmailVerifiedUseCase.checkEmailVerified()

}

object EmailVerification: RecipeBuilder<EmailVerificationConfig, EmailVerificationRecipe>() {

    override fun install(configure: EmailVerificationConfig.() -> Unit): (SuperTokensClient) -> EmailVerificationRecipe {
        val config = EmailVerificationConfig().apply(configure)

        return {
            EmailVerificationRecipe(it, config)
        }
    }

}

suspend fun SuperTokensClient.sendVerificationEmail(): Boolean {
    return getRecipe<EmailVerificationRecipe>().sendVerificationEmail()
}

suspend fun SuperTokensClient.verifyEmail(token: String): Boolean {
    return getRecipe<EmailVerificationRecipe>().verifyEmail(token)
}

suspend fun SuperTokensClient.checkEmailVerified(): Boolean {
    return getRecipe<EmailVerificationRecipe>().checkEmailVerified()
}