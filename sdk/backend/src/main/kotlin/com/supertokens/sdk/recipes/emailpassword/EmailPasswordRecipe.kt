package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.Constants
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensStatusException
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.models.FormField
import com.supertokens.sdk.recipes.common.models.Validate
import com.supertokens.sdk.recipes.emailpassword.requests.CreateResetPasswordTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignInRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignUpRequest
import com.supertokens.sdk.recipes.emailpassword.requests.ResetPasswordWithTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.UpdateUserRequest
import com.supertokens.sdk.recipes.emailpassword.responses.CreateResetPasswordTokenResponse
import com.supertokens.sdk.recipes.emailpassword.responses.ResetPasswordWithTokenResponse
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class EmailPasswordConfig: RecipeConfig {
    // The form fields to parse and validate on signup
    var formFields: List<FormField> = EmailPasswordRecipe.DEFAULT_FORM_FIELDS

    // the service to use when sending emails (password reset)
    var emailService: EmailService? = null

    // if true, validate the password before sending it to core on update
    var validatePasswordOnUpdate: Boolean = true

    // if true, validate the password before sending it to core on reset
    var validatePasswordOnReset: Boolean = true
}

class EmailPasswordRecipe(
    private val superTokens: SuperTokens,
    private val config: EmailPasswordConfig,
) : Recipe<EmailPasswordConfig> {

    val formFields: List<FormField> = config.formFields.toList()
    val emailService: EmailService? = config.emailService

    /**
     * Signup a user with email ID and password
     */
    suspend fun signUp(email: String, password: String): User {
        val response = superTokens.client.post(PATH_SIGNUP) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailPasswordSignUpRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUser().also {
            superTokens._events.tryEmit(SuperTokensEvent.UserSignUp(it))
        }
    }

    /**
     * Signin a user with email ID and password
     */
    suspend fun signIn(email: String, password: String): User {
        val response = superTokens.client.post(PATH_SIGNIN) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailPasswordSignInRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUser().also {
            superTokens._events.tryEmit(SuperTokensEvent.UserSignIn(it))
        }
    }

    /**
     * Generate a new reset password token for this user
     */
    suspend fun createResetPasswordToken(userId: String): String {
        val response = superTokens.client.post(PATH_PASSWORD_RESET_TOKEN) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateResetPasswordTokenRequest(
                    userId = userId,
                )
            )
        }

        return response.parse<CreateResetPasswordTokenResponse, String> {
            it.token
        }
    }

    /**
     * Reset a password using password reset token
     */
    suspend fun resetPasswordWithToken(token: String, newPassword: String): String {

        if(config.validatePasswordOnReset && !validatePassword(newPassword)) {
            throw SuperTokensStatusException(SuperTokensStatus.PasswordPolicyViolatedError)
        }

        val response = superTokens.client.post(PATH_PASSWORD_RESET) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ResetPasswordWithTokenRequest(
                    token = token,
                    newPassword = newPassword,
                )
            )
        }

        return response.parse<ResetPasswordWithTokenResponse, String> {
            it.userId
        }
    }

    /**
     * Update a user's email
     */
    suspend fun updateEmail(userId: String, email: String): SuperTokensStatus {
        val response = superTokens.client.put(PATH_UPDATE_USER) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UpdateUserRequest(
                    userId = userId,
                    email = email,
                )
            )
        }

        return response.parse().also {
            if(it == SuperTokensStatus.OK) {
                superTokens._events.tryEmit(SuperTokensEvent.UserEmailChanged(userId, email))
            }
        }
    }

    /**
     * Update a user's password
     */
    suspend fun updatePassword(userId: String, password: String): SuperTokensStatus {

        if(config.validatePasswordOnUpdate && !validatePassword(password)) {
            return SuperTokensStatus.PasswordPolicyViolatedError
        }

        val response = superTokens.client.put(PATH_UPDATE_USER) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                UpdateUserRequest(
                    userId = userId,
                    password = password,
                )
            )
        }

        return response.parse().also {
            if(it == SuperTokensStatus.OK) {
                superTokens._events.tryEmit(SuperTokensEvent.UserPasswordChanged(userId))
            }
        }
    }

    /**
     * Validate a password against the configured form field
     */
    fun validatePassword(password: String): Boolean {
        formFields.firstOrNull {it.id == FORM_FIELD_PASSWORD_ID}?.validate?.let {
            if(!it.invoke(password)) {
                return false
            }
        }

        return true
    }

    companion object {
        const val ID = "emailpassword"

        const val PATH_SIGNIN = "/recipe/signin"
        const val PATH_SIGNUP = "/recipe/signup"
        const val PATH_UPDATE_USER = "/recipe/user"
        const val PATH_PASSWORD_RESET_TOKEN = "/recipe/user/password/reset/token"
        const val PATH_PASSWORD_RESET = "/recipe/user/password/reset"

        const val FORM_FIELD_EMAIL_ID = "email"
        const val FORM_FIELD_PASSWORD_ID = "password"

        private val DEFAULT_PASSWORD_REGEXP = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        private val DEFAULT_PASSWORD_VALIDATOR: Validate = {
            DEFAULT_PASSWORD_REGEXP.matches(it)
        }

        private val DEFAULT_EMAIL_REGEXP = Regex("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        private val DEFAULT_EMAIL_VALIDATOR: Validate = {
            DEFAULT_EMAIL_REGEXP.matches(it)
        }

        val DEFAULT_FORM_FIELDS = listOf(
            FormField(
                id = FORM_FIELD_EMAIL_ID,
                optional = false,
                validate = DEFAULT_EMAIL_VALIDATOR,
            ),
            FormField(
                id = FORM_FIELD_PASSWORD_ID,
                optional = false,
                validate = DEFAULT_PASSWORD_VALIDATOR,
            ),
        )
    }

}

val EmailPassword = object: RecipeBuilder<EmailPasswordConfig, EmailPasswordRecipe>() {

    override fun install(configure: EmailPasswordConfig.() -> Unit): (SuperTokens) -> EmailPasswordRecipe {
        val config = EmailPasswordConfig().apply(configure)

        return {
            EmailPasswordRecipe(it, config)
        }
    }

}

/**
 * Signup a user with email ID and password
 */
suspend fun SuperTokens.emailPasswordSignUp(email: String, password: String) =
    getRecipe<EmailPasswordRecipe>().signUp(email, password)

/**
 * Signin a user with email ID and password
 */
suspend fun SuperTokens.emailPasswordSignIn(email: String, password: String) =
    getRecipe<EmailPasswordRecipe>().signIn(email, password)

/**
 * Generate a new reset password token for this user
 */
suspend fun SuperTokens.createResetPasswordToken(userId: String) =
    getRecipe<EmailPasswordRecipe>().createResetPasswordToken(userId)

/**
 * Reset a password using password reset token
 */
suspend fun SuperTokens.resetPasswordWithToken(token: String, newPassword: String) =
    getRecipe<EmailPasswordRecipe>().resetPasswordWithToken(token, newPassword)

/**
 * Update a user's email
 */
suspend fun SuperTokens.updateEmail(userId: String, email: String) =
    getRecipe<EmailPasswordRecipe>().updateEmail(userId, email)

/**
 * Update a user's password
 */
suspend fun SuperTokens.updatePassword(userId: String, password: String) =
    getRecipe<EmailPasswordRecipe>().updatePassword(userId, password)