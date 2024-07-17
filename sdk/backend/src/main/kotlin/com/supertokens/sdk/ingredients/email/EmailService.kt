package com.supertokens.sdk.ingredients.email

import com.supertokens.sdk.recipes.passwordless.models.TemplateProvider
import freemarker.cache.TemplateLoader
import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Configuration.VERSION_2_3_32
import freemarker.template.Template
import java.io.StringWriter
import java.net.URL

data class EmailContent(
    val body: String,
    val isHtml: Boolean,
    val subject: String,
    val toEmail: String,
)

abstract class EmailService(
    templateLoader: TemplateLoader = DEFAULT_EMAIL_TEMPLATE_LOADER,
    localizedLookup: Boolean = false,
    val magicLinkLoginTemplateName: String = "magic-link-login.html",
    val magicLinkOtpLoginTemplateName: String = "magic-link-otp-login.html",
    val otpLoginTemplateName: String = "otp-login.html",
    val passwordResetTemplateName: String = "password-reset.html",
    val emailVerificationTemplateName: String = "email-verification.html",
) {

  private val cfg = Configuration(VERSION_2_3_32).apply { this.localizedLookup = localizedLookup }

  init {
    cfg.templateLoader = templateLoader
  }

  abstract suspend fun sendEmail(content: EmailContent)

  open fun getTemplate(template: String): Template = cfg.getTemplate(template)

  fun processTemplate(name: String, provider: TemplateProvider): String {
    val template = getTemplate(name)
    val out = StringWriter()
    template.process(
        provider.template,
        out,
    )
    return out.toString()
  }
}

val DEFAULT_EMAIL_TEMPLATE_LOADER =
    object : URLTemplateLoader() {

      override fun getURL(name: String): URL {
        return URL(
            "https://raw.githubusercontent.com/supertokens/email-sms-templates/master/email-html/$name")
      }
    }
