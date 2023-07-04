package com.supertokens.sdk.recipes.passwordless.models

data class LoginMagicLinkOtpTemplate(
    val appname: String,
    val toEmail: String,
    val urlWithLinkCode: String,
    val otp: String,
    val time: String,
): TemplateProvider {

    override val template = mapOf(
        "appname" to appname,
        "toEmail" to toEmail,
        "urlWithLinkCode" to urlWithLinkCode,
        "otp" to otp,
        "time" to time
    )
}
