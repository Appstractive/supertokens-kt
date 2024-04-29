package com.supertokens.sdk.common

sealed class SuperTokensStatus(
    val value: String
) {

    data object OK: SuperTokensStatus("OK")

    data object EMailAlreadyExistsError: SuperTokensStatus("EMAIL_ALREADY_EXISTS_ERROR")
    data object WrongCredentialsError: SuperTokensStatus("WRONG_CREDENTIALS_ERROR")
    data object UnknownUserIdError: SuperTokensStatus("UNKNOWN_USER_ID_ERROR")
    data object UnknownEMailError: SuperTokensStatus("UNKNOWN_EMAIL_ERROR")
    data object UnknownPhoneNumberError: SuperTokensStatus("UNKNOWN_PHONE_NUMBER_ERROR")
    data object ResetPasswordInvalidTokenError: SuperTokensStatus("RESET_PASSWORD_INVALID_TOKEN_ERROR")
    data object PasswordPolicyViolatedError: SuperTokensStatus("PASSWORD_POLICY_VIOLATED_ERROR")
    data object NotFoundError: SuperTokensStatus("NOT_FOUND")
    data object InvalidApiKeyError: SuperTokensStatus("INVALID_API_KEY")
    data object UnauthorizedError: SuperTokensStatus("UNAUTHORISED")
    data object TryRefreshTokenError: SuperTokensStatus("TRY_REFRESH_TOKEN")
    data object FormFieldError: SuperTokensStatus("FIELD_ERROR")
    data object EmailAlreadyVerifiedError: SuperTokensStatus("EMAIL_ALREADY_VERIFIED_ERROR")
    data object PasswordlessRestartFlowError: SuperTokensStatus("RESTART_FLOW_ERROR")
    data object PasswordlessCodeAlreadyUsedError: SuperTokensStatus("USER_INPUT_CODE_ALREADY_USED_ERROR")
    data object PasswordlessIncorrectCodeError: SuperTokensStatus("INCORRECT_USER_INPUT_CODE_ERROR")
    data object PasswordlessExpiredCodeError: SuperTokensStatus("EXPIRED_USER_INPUT_CODE_ERROR")
    data object TotpDeviceAlreadyExistsError: SuperTokensStatus("DEVICE_ALREADY_EXISTS_ERROR")
    data object TotpNotEnabledError: SuperTokensStatus("TOTP_NOT_ENABLED_ERROR")
    data object TotpDeviceUnknownError: SuperTokensStatus("UNKNOWN_DEVICE_ERROR")
    data object InvalidTotpCodeError: SuperTokensStatus("INVALID_TOTP_ERROR")
    data object TotpLimitReachedError: SuperTokensStatus("LIMIT_REACHED_ERROR")
    data object AccountInfoAlreadyAssociatedError: SuperTokensStatus("ACCOUNT_INFO_ALREADY_ASSOCIATED_WITH_ANOTHER_PRIMARY_USER_ID_ERROR")
    data object RecipeUserIdAlreadyLinkedWithAnotherPrimaryUserError: SuperTokensStatus("RECIPE_USER_ID_ALREADY_LINKED_WITH_ANOTHER_PRIMARY_USER_ID_ERROR")
    data object RecipeUserIdAlreadyLinkedError: SuperTokensStatus("RECIPE_USER_ID_ALREADY_LINKED_WITH_PRIMARY_USER_ID_ERROR")
    data object NotPrimaryUserError: SuperTokensStatus("INPUT_USER_IS_NOT_A_PRIMARY_USER")
    class AppIdOrTenantIdNotFoundError(message: String): SuperTokensStatus(message)

    data object UnknownError: SuperTokensStatus("UNKNOWN_ERROR")

}

fun String.toStatus(): SuperTokensStatus {
    when {
        startsWith("AppId or tenantId not found") -> return SuperTokensStatus.AppIdOrTenantIdNotFoundError(this)
    }

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
        SuperTokensStatus.EmailAlreadyVerifiedError.value -> SuperTokensStatus.EmailAlreadyVerifiedError
        SuperTokensStatus.PasswordlessRestartFlowError.value -> SuperTokensStatus.PasswordlessRestartFlowError
        SuperTokensStatus.PasswordlessCodeAlreadyUsedError.value -> SuperTokensStatus.PasswordlessCodeAlreadyUsedError
        SuperTokensStatus.PasswordlessIncorrectCodeError.value -> SuperTokensStatus.PasswordlessIncorrectCodeError
        SuperTokensStatus.PasswordlessExpiredCodeError.value -> SuperTokensStatus.PasswordlessExpiredCodeError
        SuperTokensStatus.UnknownPhoneNumberError.value -> SuperTokensStatus.UnknownPhoneNumberError
        SuperTokensStatus.TotpDeviceAlreadyExistsError.value -> SuperTokensStatus.TotpDeviceAlreadyExistsError
        SuperTokensStatus.TotpNotEnabledError.value -> SuperTokensStatus.TotpNotEnabledError
        SuperTokensStatus.TotpDeviceUnknownError.value -> SuperTokensStatus.TotpDeviceUnknownError
        SuperTokensStatus.InvalidTotpCodeError.value -> SuperTokensStatus.InvalidTotpCodeError
        SuperTokensStatus.TotpLimitReachedError.value -> SuperTokensStatus.TotpLimitReachedError
        else -> SuperTokensStatus.UnknownError
    }
}