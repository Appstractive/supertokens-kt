package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.parseUserResponse
import com.supertokens.sdk.recipes.emailpassword.requests.CreateResetPasswordTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignInRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignUpRequest
import com.supertokens.sdk.recipes.emailpassword.requests.ResetPasswordWithTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.UpdateUserRequest
import com.supertokens.sdk.recipes.emailpassword.responses.CreateResetPasswordTokenResponse
import com.supertokens.sdk.recipes.emailpassword.responses.ResetPasswordWithTokenResponse
import com.supertokens.sdk.toStatus
import com.supertokens.sdk.utils.parse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import it.czerwinski.kotlin.util.Either
import it.czerwinski.kotlin.util.Left
import it.czerwinski.kotlin.util.Right

typealias Validate = (value: String) -> Boolean

data class FormField(
    val id: String,
    val optional: Boolean = true,
    val validate: Validate? = null
) {

    companion object {
        const val FORM_FIELD_EMAIL_ID = "email"
        const val FORM_FIELD_PASSWORD_ID = "password"

        private val DEFAULT_PASSWORD_REGEXP = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        val DEFAULT_PASSWORD_VALIDATOR: Validate = {
            DEFAULT_PASSWORD_REGEXP.matches(it)
        }

        private val DEFAULT_EMAIL_REGEXP = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,6}")
        val DEFAULT_EMAIL_VALIDATOR: Validate = {
            DEFAULT_EMAIL_REGEXP.matches(it)
        }
    }

}

class EmailPasswordConfig: RecipeConfig

class EmailPasswordRecipe(
    private val superTokens: SuperTokens,
    private val emailPasswordConfig: EmailPasswordConfig,
) : Recipe<EmailPasswordConfig> {

    var formFields: List<FormField> = listOf(
        FormField(
            id = FormField.FORM_FIELD_EMAIL_ID,
            optional = false,
            validate = FormField.DEFAULT_EMAIL_VALIDATOR,
        ),
        FormField(
            id = FormField.FORM_FIELD_PASSWORD_ID,
            optional = false,
            validate = FormField.DEFAULT_PASSWORD_VALIDATOR,
        ),
    )

    suspend fun signUp(email: String, password: String): Either<SuperTokensStatus, User> {
        val response = superTokens.client.post(PATH_SIGNUP) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailPasswordSignUpRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUserResponse()
    }

    suspend fun signIn(email: String, password: String): Either<SuperTokensStatus, User> {
        val response = superTokens.client.post(PATH_SIGNIN) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                EmailPasswordSignInRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUserResponse()
    }

    suspend fun getUserById(userId: String): Either<SuperTokensStatus, User> {

        val response = superTokens.client.get("$PATH_GET_USER?userId=$userId") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parseUserResponse()
    }

    suspend fun getUserByEMail(email: String): Either<SuperTokensStatus, User> {

        val response = superTokens.client.get("$PATH_GET_USER?email=$email") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parseUserResponse()
    }

    suspend fun createResetPasswordToken(userId: String): Either<SuperTokensStatus, String> {
        val response = superTokens.client.post(PATH_PASSWORD_RESET_TOKEN) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreateResetPasswordTokenRequest(
                    userId = userId,
                )
            )
        }

        if (response.status != HttpStatusCode.OK) {
            return Left(response.bodyAsText().toStatus())
        }

        val body = response.body<CreateResetPasswordTokenResponse>()

        return when (val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> Right(body.token)
            else -> Left(status)
        }
    }

    suspend fun resetPasswordWithToken(token: String, newPassword: String): Either<SuperTokensStatus, String> {
        val response = superTokens.client.post(PATH_PASSWORD_RESET) {
            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ResetPasswordWithTokenRequest(
                    token = token,
                    newPassword = newPassword,
                )
            )
        }

        if (response.status != HttpStatusCode.OK) {
            return Left(response.bodyAsText().toStatus())
        }

        val body = response.body<ResetPasswordWithTokenResponse>()

        return when (val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> Right(body.userId)
            else -> Left(status)
        }
    }

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

        return response.parse()
    }

    suspend fun updatePassword(userId: String, password: String, applyPasswordPolicy: Boolean = true): SuperTokensStatus {

        if(applyPasswordPolicy) {
            formFields.firstOrNull {it.id == FormField.FORM_FIELD_PASSWORD_ID}?.validate?.let {
                if(!it.invoke(password)) {
                    return SuperTokensStatus.PasswordPolicyViolatedError
                }
            }
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

        return response.parse()
    }

    companion object {
        const val ID = "emailpassword"

        const val PATH_SIGNIN = "recipe/signin"
        const val PATH_SIGNUP = "recipe/signup"
        const val PATH_GET_USER = "recipe/user"
        const val PATH_UPDATE_USER = "recipe/user"
        const val PATH_PASSWORD_RESET_TOKEN = "recipe/user/password/reset/token"
        const val PATH_PASSWORD_RESET = "recipe/user/password/reset"
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

suspend fun SuperTokens.emailPasswordSignUp(email: String, password: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().signUp(email, password)
}

suspend fun SuperTokens.emailPasswordSignIn(email: String, password: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().signIn(email, password)
}

suspend fun SuperTokens.emailPasswordGetUserById(userId: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().getUserById(userId)
}

suspend fun SuperTokens.getUserByEmail(email: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().getUserByEMail(email)
}

suspend fun SuperTokens.createResetPasswordToken(userId: String): Either<SuperTokensStatus, String> {
    return getRecipe<EmailPasswordRecipe>().createResetPasswordToken(userId)
}

suspend fun SuperTokens.resetPasswordWithToken(token: String, newPassword: String): Either<SuperTokensStatus, String> {
    return getRecipe<EmailPasswordRecipe>().resetPasswordWithToken(token, newPassword)
}

suspend fun SuperTokens.updateEmail(userId: String, email: String): SuperTokensStatus {
    return getRecipe<EmailPasswordRecipe>().updateEmail(userId, email)
}

suspend fun SuperTokens.updatePassword(userId: String, password: String, applyPasswordPolicy: Boolean = true): SuperTokensStatus {
    return getRecipe<EmailPasswordRecipe>().updatePassword(userId, password, applyPasswordPolicy)
}