package com.supertokens.sdk

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

    data class UnknownError(val message: String): SuperTokensStatus(message)

}

fun String.toStatus(): SuperTokensStatus {
    return when(this) {
        SuperTokensStatus.WrongCredentialsError.value -> SuperTokensStatus.WrongCredentialsError
        SuperTokensStatus.EMailAlreadyExistsError.value -> SuperTokensStatus.EMailAlreadyExistsError
        SuperTokensStatus.UnknownUserIdError.value -> SuperTokensStatus.UnknownUserIdError
        SuperTokensStatus.UnknownEMailError.value -> SuperTokensStatus.UnknownEMailError
        SuperTokensStatus.ResetPasswordInvalidTokenError.value -> SuperTokensStatus.ResetPasswordInvalidTokenError
        SuperTokensStatus.PasswordPolicyViolatedError.value -> SuperTokensStatus.PasswordPolicyViolatedError
        else -> SuperTokensStatus.UnknownError(this)
    }
}