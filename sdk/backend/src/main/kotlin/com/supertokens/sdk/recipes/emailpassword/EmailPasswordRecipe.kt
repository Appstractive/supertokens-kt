package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.SuperTokensStatus
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.models.User
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.common.StatusResponse
import com.supertokens.sdk.recipes.common.parseUserResponse
import com.supertokens.sdk.recipes.emailpassword.requests.CreateResetPasswordTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignInRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignUpRequest
import com.supertokens.sdk.recipes.emailpassword.requests.ResetPasswordWithTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.UpdateUserRequest
import com.supertokens.sdk.recipes.emailpassword.responses.CreateResetPasswordTokenResponse
import com.supertokens.sdk.recipes.emailpassword.responses.ResetPasswordWithTokenResponse
import com.supertokens.sdk.toStatus
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

class EmailPasswordRecipe : Recipe {

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

    suspend fun signUp(superTokens: SuperTokens, email: String, password: String): Either<SuperTokensStatus, User> {
        val response = superTokens.client.post(PATH_SIGNUP) {

            header("rid", ID)

            setBody(
                EmailPasswordSignUpRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUserResponse()
    }

    suspend fun signIn(superTokens: SuperTokens, email: String, password: String): Either<SuperTokensStatus, User> {
        val response = superTokens.client.post(PATH_SIGNIN) {

            header("rid", ID)

            setBody(
                EmailPasswordSignInRequest(
                    email = email,
                    password = password
                )
            )
        }

        return response.parseUserResponse()
    }

    suspend fun getUserById(superTokens: SuperTokens, userId: String): Either<SuperTokensStatus, User> {

        val response = superTokens.client.get("$PATH_GET_USER?userId=$userId") {
            header("rid", ID)
        }

        return response.parseUserResponse()
    }

    suspend fun getUserByEMail(superTokens: SuperTokens, email: String): Either<SuperTokensStatus, User> {

        val response = superTokens.client.get("$PATH_GET_USER?email=$email") {
            header("rid", ID)
        }

        return response.parseUserResponse()
    }

    suspend fun createResetPasswordToken(superTokens: SuperTokens, userId: String): Either<SuperTokensStatus, String> {
        val response = superTokens.client.post(PATH_PASSWORD_RESET_TOKEN) {
            header("rid", ID)

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

    suspend fun resetPasswordWithToken(superTokens: SuperTokens, token: String, newPassword: String): Either<SuperTokensStatus, String> {
        val response = superTokens.client.post(PATH_PASSWORD_RESET) {
            header("rid", ID)

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

    suspend fun updateEmail(superTokens: SuperTokens, userId: String, email: String): SuperTokensStatus {
        val response = superTokens.client.put(PATH_UPDATE_USER) {
            header("rid", ID)

            setBody(
                UpdateUserRequest(
                    userId = userId,
                    email = email,
                )
            )
        }

        if (response.status != HttpStatusCode.OK) {
            return response.bodyAsText().toStatus()
        }

        val body = response.body<StatusResponse>()

        return body.status.toStatus()
    }

    suspend fun updatePassword(superTokens: SuperTokens, userId: String, password: String, applyPasswordPolicy: Boolean = true): SuperTokensStatus {

        if(applyPasswordPolicy) {
            formFields.firstOrNull {it.id == FormField.FORM_FIELD_PASSWORD_ID}?.validate?.let {
                if(!it.invoke(password)) {
                    return SuperTokensStatus.PasswordPolicyViolatedError
                }
            }
        }

        val response = superTokens.client.put(PATH_UPDATE_USER) {
            header("rid", ID)

            setBody(
                UpdateUserRequest(
                    userId = userId,
                    password = password,
                )
            )
        }

        if (response.status != HttpStatusCode.OK) {
            return response.bodyAsText().toStatus()
        }

        val body = response.body<StatusResponse>()

        return body.status.toStatus()
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

fun SuperTokensConfig.emailPassword(init: EmailPasswordRecipe.() -> Unit) {
    val recipe = EmailPasswordRecipe()
    recipe.init()
    +recipe
}

suspend fun SuperTokens.emailPasswordSignUp(email: String, password: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().signUp(this, email, password)
}

suspend fun SuperTokens.emailPasswordSignIn(email: String, password: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().signIn(this, email, password)
}

suspend fun SuperTokens.emailPasswordGetUserById(userId: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().getUserById(this, userId)
}

suspend fun SuperTokens.emailPasswordGetUserByEmail(email: String): Either<SuperTokensStatus, User> {
    return getRecipe<EmailPasswordRecipe>().getUserByEMail(this, email)
}

suspend fun SuperTokens.emailPasswordCreateResetPasswordToken(userId: String): Either<SuperTokensStatus, String> {
    return getRecipe<EmailPasswordRecipe>().createResetPasswordToken(this, userId)
}

suspend fun SuperTokens.emailPasswordResetPasswordWithToken(token: String, newPassword: String): Either<SuperTokensStatus, String> {
    return getRecipe<EmailPasswordRecipe>().resetPasswordWithToken(this, token, newPassword)
}

suspend fun SuperTokens.emailPasswordUpdateEmail(userId: String, email: String): SuperTokensStatus {
    return getRecipe<EmailPasswordRecipe>().updateEmail(this, userId, email)
}

suspend fun SuperTokens.emailPasswordUpdatePassword(userId: String, password: String, applyPasswordPolicy: Boolean = true): SuperTokensStatus {
    return getRecipe<EmailPasswordRecipe>().updatePassword(this, userId, password, applyPasswordPolicy)
}