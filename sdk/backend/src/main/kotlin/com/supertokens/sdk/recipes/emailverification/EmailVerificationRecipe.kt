package com.supertokens.sdk.recipes.emailverification

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.emailverification.requests.EmailVerificationRequest
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequest
import com.supertokens.sdk.recipes.emailverification.models.VerifyEmailTokenData
import com.supertokens.sdk.recipes.emailverification.responses.CreateEmailVerificationTokenResponse
import com.supertokens.sdk.common.responses.VerifyEmailResponse
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.recipes.emailverification.responses.VerifyEmailTokenResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class EmailVerificationRecipeConfig: RecipeConfig {

    var emailService: EmailService? = null

}

class EmailVerificationRecipe(
    private val superTokens: SuperTokens,
    private val config: EmailVerificationRecipeConfig
): Recipe<EmailVerificationRecipeConfig> {

    val emailService: EmailService? = config.emailService

    suspend fun setVerified(userId: String, email: String) {
        val token = createVerificationToken(userId, email)
        verifyToken(token)
    }

    suspend fun isVerified(userId: String, email: String?): Boolean {
        return email?.let {
            val response  = runCatching {
                checkEmailVerified(userId, it)
            }
            response.getOrElse {
                true
            }
        // users without an email are verified (to have none)
        } ?: true
    }

    override suspend fun getExtraJwtData(user: User): Map<String, Any?> {
        return buildMap {
            set("st-ev", isVerified(user.id, user.email))
        }
    }

    suspend fun createVerificationToken(userId: String, email: String): String {
        val response = superTokens.client.post(PATH_CREATE_TOKEN) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailVerificationRequest(
                    userId = userId,
                    email = email,
                )
            )
        }

        return response.parse<CreateEmailVerificationTokenResponse, String> {
            it.token ?: throw SuperTokensStatusException(SuperTokensStatus.EmailAlreadyVerifiedError)
        }
    }

    suspend fun removeAllVerificationTokens(userId: String, email: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_DELETE_TOKENS) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailVerificationRequest(
                    userId = userId,
                    email = email,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun verifyToken(token: String): VerifyEmailTokenData {
        val response = superTokens.client.post(PATH_VERIFY) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                VerifyEmailTokenRequest(
                    token = token,
                )
            )
        }

        return response.parse<VerifyEmailTokenResponse, VerifyEmailTokenData> {
            VerifyEmailTokenData(
                userId = it.userId,
                email = it.email,
            )
        }.also {
            superTokens._events.tryEmit(SuperTokensEvent.UserEmailVerified(it.userId, it.email))
        }
    }

    suspend fun checkEmailVerified(userId: String, email: String): Boolean {
        val response = superTokens.client.get("$PATH_VERIFY?userId=$userId&email=$email") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<VerifyEmailResponse, Boolean> {
            it.isVerified
        }
    }

    suspend fun setUnverified(userId: String, email: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_VERIFY_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailVerificationRequest(
                    userId = userId,
                    email = email,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }.also {
            superTokens._events.tryEmit(SuperTokensEvent.UserEmailUnVerified(userId, email))
        }
    }

    companion object {
        const val ID = "emailverification"

        const val PATH_CREATE_TOKEN = "/recipe/user/email/verify/token"
        const val PATH_DELETE_TOKENS = "/recipe/user/email/verify/token/remove"
        const val PATH_VERIFY = "/recipe/user/email/verify"
        const val PATH_VERIFY_REMOVE = "/recipe/user/email/verify/remove"
    }

}

val EmailVerification = object: RecipeBuilder<EmailVerificationRecipeConfig, EmailVerificationRecipe>() {

    override fun install(configure: EmailVerificationRecipeConfig.() -> Unit): (SuperTokens) -> EmailVerificationRecipe {
        val config = EmailVerificationRecipeConfig().apply(configure)

        return {
            EmailVerificationRecipe(it, config)
        }
    }

}

suspend fun SuperTokens.createEmailVerificationToken(
    userId: String,
    email: String,
) = getRecipe<EmailVerificationRecipe>().createVerificationToken(userId, email)

suspend fun SuperTokens.removeAllVerificationTokens(
    userId: String,
    email: String,
) = getRecipe<EmailVerificationRecipe>().removeAllVerificationTokens(userId, email)

suspend fun SuperTokens.verifyToken(
    token: String,
) = getRecipe<EmailVerificationRecipe>().verifyToken(token)

suspend fun SuperTokens.checkEmailVerified(
    userId: String,
    email: String,
) = getRecipe<EmailVerificationRecipe>().checkEmailVerified(userId, email)

suspend fun SuperTokens.setUnverified(
    userId: String,
    email: String,
) = getRecipe<EmailVerificationRecipe>().setUnverified(userId, email)