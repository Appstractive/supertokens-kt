package com.supertokens.sdk.recipes.emailverification.models

import com.supertokens.sdk.recipes.passwordless.models.TemplateProvider

data class EmailVerificationTemplate(
    val appName: String,
    val email: String,
    val verificationLink: String,
) : TemplateProvider {
  override val template: Map<String, String> =
      mapOf(
          "appName" to appName,
          "email" to email,
          "verificationLink" to verificationLink,
      )
}
