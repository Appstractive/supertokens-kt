package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.accessToken
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.recipes.session.isSessionsEnabled
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.superTokens
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.frontend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.EndpointConfig
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_EMAIL_VERIFICATION
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.responses.VerifyEmailResponseDTO
import com.supertokens.sdk.core.getUserByEMailOrNull
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.recipes.emailverification.models.EmailVerificationTemplate
import com.supertokens.sdk.recipes.session.getSessions
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class EmailVerificationHandler(
  protected val scope: CoroutineScope,
) {

  open suspend fun RoutingContext.createVerificationLink(
    frontend: EndpointConfig,
    token: String
  ) = "${frontend.fullUrl}verify-email?token=$token"

  /** Override this to send localized mails */
  open suspend fun RoutingContext.getResetPasswordTemplateName(
    emailService: EmailService
  ) = emailService.emailVerificationTemplateName

  open suspend fun RoutingContext.sendVerificationMail(email: String) {
    val frontend = call.frontend

    emailVerification.emailService?.let {
      // launch the email sending in another scope, so the call is not blocked
      scope.launch {
        runCatching {
          val user = superTokens.getUserByEMailOrNull(email) ?: return@launch
          val token =
              emailVerification.createVerificationToken(
                  userId = user.id,
                  email = email,
                  tenantId = call.tenantId,
              )

          val body =
              it.processTemplate(
                  getResetPasswordTemplateName(it),
                  EmailVerificationTemplate(
                      appName = superTokens.appConfig.name,
                      email = email,
                      verificationLink = createVerificationLink(frontend, token),
                  ),
              )

          it.sendEmail(
              EmailContent(
                  body = body,
                  isHtml = true,
                  subject = superTokens.appConfig.name,
                  toEmail = email,
              ),
          )
        }
      }
    }
  }

  /**
   * A call to POST /user/email/verify/token
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/verifyEmailToken">Frontend
   *   Driver Interface</a>
   */
  open suspend fun RoutingContext.sendEmailVerification() {
    val principal = call.requirePrincipal<AuthenticatedUser>()
    val user = superTokens.getUserById(principal.id)

    user.loginMethods?.forEach {
      if (it.recipeId == RECIPE_EMAIL_PASSWORD && !it.verified) {
        it.email?.let { email -> sendVerificationMail(email) }
      }
    }

    call.respond(StatusResponseDTO())
  }

  /**
   * A call to POST /user/email/verify
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/verifyEmail">Frontend
   *   Driver Interface</a>
   */
  open suspend fun RoutingContext.verifyEmail() {
    val body = call.receive<VerifyEmailTokenRequestDTO>()
    val tenantId = call.tenantId

    when (body.method) {
      "token" -> {
        val data = emailVerification.verifyToken(body.token)

        // update email verification state in existing sessions
        val jwtData =
            sessions.getJwtData(
                user = superTokens.getUserById(data.userId),
                tenantId = tenantId,
                recipeId = RECIPE_EMAIL_VERIFICATION,
                multiAuthFactor = null,
                accessToken = accessToken,
            )

        val userSessions = superTokens.getSessions(data.userId)
        userSessions.forEach {
          sessions.updateJwtData(sessionHandle = it, userDataInJWT = jwtData, tenantId = tenantId)
        }

        // update token if present and from same user
        runCatching {
          accessToken?.let { token ->
            val session = sessions.verifySession(token, checkDatabase = true)

            if (session.session.userId == data.userId) {

              val newSession = sessions.regenerateSession(token, jwtData)

              setSessionInResponse(
                  accessToken = newSession.accessToken,
              )
            }
          }
        }

        call.respond(StatusResponseDTO())
      }

      else -> throw BadRequestException("Invalid verification method ${body.method}")
    }
  }

  /**
   * A call to GET /user/email/verify
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailVerification%20Recipe/getVerifyEmail">Frontend
   *   Driver Interface</a>
   */
  open suspend fun RoutingContext.checkEmailVerified() {
    val user = call.requirePrincipal<AuthenticatedUser>()
    val email = superTokens.getUserById(user.id).email
    val isVerified = emailVerification.isVerified(user.id, email)

    if (isSessionsEnabled && isVerified) {
      val session =
          sessions.regenerateSession(
              accessToken = user.accessToken,
              userDataInJWT =
                  sessions.getJwtData(
                      user = superTokens.getUserById(user.id),
                      tenantId = call.tenantId,
                      recipeId = RECIPE_EMAIL_VERIFICATION,
                      multiAuthFactor = null,
                      accessToken = accessToken,
                  ),
          )
      setSessionInResponse(
          accessToken = session.accessToken,
      )
    }

    call.respond(
            VerifyEmailResponseDTO(
                    isVerified = isVerified,
            ),
    )
  }
}
