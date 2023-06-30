package com.supertokens.sdk.common

sealed class SuperTokensStatus(
    val value: String
) {

    object OK: SuperTokensStatus("OK")

    object EMailAlreadyExistsError: SuperTokensStatus("EMAIL_ALREADY_EXISTS_ERROR")
    object WrongCredentialsError: SuperTokensStatus("WRONG_CREDENTIALS_ERROR")
    object UnknownUserIdError: SuperTokensStatus("UNKNOWN_USER_ID_ERROR")
    object UnknownEMailError: SuperTokensStatus("UNKNOWN_EMAIL_ERROR")
    object ResetPasswordInvalidTokenError: SuperTokensStatus("RESET_PASSWORD_INVALID_TOKEN_ERROR")
    object PasswordPolicyViolatedError: SuperTokensStatus("PASSWORD_POLICY_VIOLATED_ERROR")
    object NotFoundError: SuperTokensStatus("Not Found")
    object InvalidApiKeyError: SuperTokensStatus("Invalid API key")
    object UnauthorizedError: SuperTokensStatus("UNAUTHORISED")
    object TryRefreshTokenError: SuperTokensStatus("TRY_REFRESH_TOKEN")
    object FormFieldError: SuperTokensStatus("FIELD_ERROR")

    data class UnknownError(val message: String): SuperTokensStatus(message)

}

fun String.toStatus(): SuperTokensStatus {
    return when(this) {
        SuperTokensStatus.OK.value -> SuperTokensStatus.OK
        SuperTokensStatus.WrongCredentialsError.value -> SuperTokensStatus.WrongCredentialsError
        SuperTokensStatus.EMailAlreadyExistsError.value -> SuperTokensStatus.EMailAlreadyExistsError
        SuperTokensStatus.UnknownUserIdError.value -> SuperTokensStatus.UnknownUserIdError
        SuperTokensStatus.UnknownEMailError.value -> SuperTokensStatus.UnknownEMailError
        SuperTokensStatus.ResetPasswordInvalidTokenError.value -> SuperTokensStatus.ResetPasswordInvalidTokenError
        SuperTokensStatus.PasswordPolicyViolatedError.value -> SuperTokensStatus.PasswordPolicyViolatedError
        SuperTokensStatus.NotFoundError.value -> SuperTokensStatus.NotFoundError
        SuperTokensStatus.InvalidApiKeyError.value -> SuperTokensStatus.InvalidApiKeyError
        SuperTokensStatus.UnauthorizedError.value -> SuperTokensStatus.UnauthorizedError
        SuperTokensStatus.TryRefreshTokenError.value -> SuperTokensStatus.TryRefreshTokenError
        SuperTokensStatus.FormFieldError.value -> SuperTokensStatus.FormFieldError
        else -> SuperTokensStatus.UnknownError(this)
    }
}