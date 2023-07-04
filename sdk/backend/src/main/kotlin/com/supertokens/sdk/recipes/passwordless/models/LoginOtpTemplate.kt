package com.supertokens.sdk.recipes.passwordless.models

data class LoginOtpTemplate(
    val appname: String,
    val toEmail: String,
    val otp: String,
    val time: String,
): TemplateProvider {

    override val template = mapOf(
        "appname" to appname,
        "toEmail" to toEmail,
        "otp" to otp,
        "time" to time
    )
}
