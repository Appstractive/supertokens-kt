package com.supertokens.sdk.recipes.emailpassword.models

import com.supertokens.sdk.recipes.passwordless.models.TemplateProvider

data class EmailResetTemplate(
    val appname: String,
    val toEmail: String,
    val resetLink: String,
) : TemplateProvider {
  override val template: Map<String, String> =
      mapOf(
          "appname" to appname,
          "toEmail" to toEmail,
          "resetLink" to resetLink,
      )
}
