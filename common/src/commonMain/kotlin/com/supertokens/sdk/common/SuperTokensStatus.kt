package com.supertokens.sdk.common

sealed class SuperTokensStatus(
    val value: String
) {

    object OK: SuperTokensStatus("OK")

    object EMailAlreadyExistsError: SuperTokensStatus("EMAIL_ALREADY_EXISTS_ERROR")
    object WrongCredentialsError: SuperTokensStatus("WRONG_CREDENTIALS_ERROR")
    object UnknownUserIdError: SuperTokensStatus("UNKNOWN_USER_ID_ERROR")
    object UnknownEMailError: SuperTokensStatus("UNKNOWN_EMAIL_ERROR")
    object UnknownPhoneNumberError: SuperTokensStatus("UNKNOWN_PHONE_NUMBER_ERROR")
    object ResetPasswordInvalidTokenError: SuperTokensStatus("RESET_PASSWORD_INVALID_TOKEN_ERROR")
    object PasswordPolicyViolatedError: SuperTokensStatus("PASSWORD_POLICY_VIOLATED_ERROR")
    object NotFoundError: SuperTokensStatus("NOT_FOUND")
    object InvalidApiKeyError: SuperTokensStatus("INVALID_API_KEY")
    object UnauthorizedError: SuperTokensStatus("UNAUTHORISED")
    object TryRefreshTokenError: SuperTokensStatus("TRY_REFRESH_TOKEN")
    object FormFieldError: SuperTokensStatus("FIELD_ERROR")
    object EmailAlreadyVerifiedError: SuperTokensStatus("EMAIL_ALREADY_VERIFIED_ERROR")
    object PasswordlessRestartFlowError: SuperTokensStatus("RESTART_FLOW_ERROR")
    object PasswordlessCodeAlreadyUsedError: SuperTokensStatus("USER_INPUT_CODE_ALREADY_USED_ERROR")
    object PasswordlessIncorrectCodeError: SuperTokensStatus("INCORRECT_USER_INPUT_CODE_ERROR")
    object PasswordlessExpiredCodeError: SuperTokensStatus("EXPIRED_USER_INPUT_CODE_ERROR")
    object TotpDeviceAlreadyExistsError: SuperTokensStatus("DEVICE_ALREADY_EXISTS_ERROR")
    object TotpNotEnabledError: SuperTokensStatus("TOTP_NOT_ENABLED_ERROR")
    object TotpDeviceUnknownError: SuperTokensStatus("UNKNOWN_DEVICE_ERROR")
    object InvalidTotpCodeError: SuperTokensStatus("INVALID_TOTP_ERROR")
    object TotpLimitReachedError: SuperTokensStatus("LIMIT_REACHED_ERROR")
    class AppIdOrTenantIdNotFoundError(message: String): SuperTokensStatus(message)

    object UnknownError: SuperTokensStatus("UNKNOWN_ERROR")

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