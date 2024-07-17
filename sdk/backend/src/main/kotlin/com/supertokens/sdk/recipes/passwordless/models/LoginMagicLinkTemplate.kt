package com.supertokens.sdk.recipes.passwordless.models

data class LoginMagicLinkTemplate(
    val appname: String,
    val toEmail: String,
    val urlWithLinkCode: String,
    val time: String,
) : TemplateProvider {

  override val template =
      mapOf(
          "appname" to appname,
          "toEmail" to toEmail,
          "urlWithLinkCode" to urlWithLinkCode,
          "time" to time)
}
