package com.supertokens.ktor

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.interfaces.Verification
import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.TokenValidator
import com.supertokens.ktor.plugins.authHeaderCookieWrapper
import com.supertokens.ktor.plugins.roleBased
import com.supertokens.ktor.recipes.emailpassword.EmailPasswordHandler
import com.supertokens.ktor.recipes.emailpassword.emailPasswordRoutes
import com.supertokens.ktor.recipes.emailpassword.isEmailPasswordEnabled
import com.supertokens.ktor.recipes.emailverification.EmailVerificationHandler
import com.supertokens.ktor.recipes.emailverification.emailVerificationRoutes
import com.supertokens.ktor.recipes.emailverification.isEmailVerificationEnabled
import com.supertokens.ktor.recipes.multifactor.MfaHandlerAttributeKey
import com.supertokens.ktor.recipes.multifactor.MultiFactorHandler
import com.supertokens.ktor.recipes.multifactor.isMultiFactorAuthEnabled
import com.supertokens.ktor.recipes.multifactor.multiFactorRoutes
import com.supertokens.ktor.recipes.passwordless.PasswordlessHandler
import com.supertokens.ktor.recipes.passwordless.isPasswordlessEnabled
import com.supertokens.ktor.recipes.passwordless.passwordlessRoutes
import com.supertokens.ktor.recipes.session.SessionHandler
import com.supertokens.ktor.recipes.session.isSessionsEnabled
import com.supertokens.ktor.recipes.session.sessionRoutes
import com.supertokens.ktor.recipes.thirdparty.ThirdPartyHandler
import com.supertokens.ktor.recipes.thirdparty.isThirdPartyEnabled
import com.supertokens.ktor.recipes.thirdparty.thirdPartyRoutes
import com.supertokens.ktor.recipes.totp.TotpHandler
import com.supertokens.ktor.recipes.totp.isTotpEnabled
import com.supertokens.ktor.recipes.totp.totpRoutes
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuthRecipe
import com.supertokens.sdk.recipes.roles.RolesRecipe
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

typealias JwtVerification = Verification.() -> Unit

@SuperTokensKtorDslMarker
class SuperTokensConfig {

  // The SuperTokens instance to use (required)
  var superTokens: SuperTokens? = null

  var scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  // Handler for core APIs
  var coreHandler: CoreHandler = CoreHandler(scope = scope)

  var userHandler: UserHandler = UserHandler(scope = scope)

  // Handler for EmailPassword APIs
  var emailPasswordHandler: EmailPasswordHandler = EmailPasswordHandler(scope = scope)

  // Handler for Session APIs
  var sessionHandler: SessionHandler = SessionHandler(scope = scope)

  // Handler for ThirdParty APIs
  var thirdPartyHandler: ThirdPartyHandler = ThirdPartyHandler(scope = scope)

  // Handler for EmailVerification APIs
  var emailVerificationHandler: EmailVerificationHandler = EmailVerificationHandler(scope = scope)

  // Handler for Passwordless APIs
  var passwordlessHandler: PasswordlessHandler = PasswordlessHandler(scope = scope)

  // Handler for Totp APIs
  var totpHandler: TotpHandler = TotpHandler(scope = scope)

  var mfaHandler: MultiFactorHandler = MultiFactorHandler(scope = scope)

  // Allows you to perform additional validations on the JWT payload.
  var jwtValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = TokenValidator

  internal var jwtVerification: JwtVerification? = null

  // used to verify a token format and signature
  fun jwtVerification(verify: JwtVerification) {
    jwtVerification = verify
  }
}

val SuperTokens =
    createApplicationPlugin(name = "SuperTokens", createConfiguration = ::SuperTokensConfig) {
      val config = pluginConfig
      val superTokens =
          config.superTokens ?: throw RuntimeException("SuperTokens SDK not configured")

      application.attributes.put(SuperTokensAttributeKey, superTokens)
      application.attributes.put(UserHandlerAttributeKey, config.userHandler)

      if (superTokens.hasRecipe<MultiFactorAuthRecipe>()) {
        application.attributes.put(MfaHandlerAttributeKey, config.mfaHandler)
      }

      application.routing {
        route(superTokens.appConfig.api.basePath) {
          install(ContentNegotiation) {
            json(
                Json {
                  prettyPrint = true
                  isLenient = true
                })
          }

          if (superTokens.hasRecipe<SessionRecipe>()) {
            application.install(Authentication) {
              jwt(name = SuperTokensAuth) {
                validate(config.jwtValidator)

                authHeader(authHeaderCookieWrapper)

                verifier(
                    UrlJwkProvider(URL(superTokens.jwksUrl)),
                    superTokens.getRecipe<SessionRecipe>().issuer) {
                      config.jwtVerification?.invoke(this)
                    }
              }

              if (superTokens.hasRecipe<RolesRecipe>()) {
                roleBased {
                  extractRoles { principal ->
                    (principal as? AuthenticatedUser)?.roles ?: emptySet()
                  }
                  extractPermissions { principal ->
                    (principal as? AuthenticatedUser)?.permissions ?: emptySet()
                  }
                }
              }
            }
          }

          recipeRoutes(superTokens, config)

          // we need to install the routes for the optional tenantId again,
          // because optional path parameters are only allowed at the end of a path
          route("{tenantId?}/") { recipeRoutes(superTokens, config) }
        }
      }
    }

private fun Route.recipeRoutes(superTokens: SuperTokens, config: SuperTokensConfig) {
  coreRoutes(config.coreHandler)

  if (isSessionsEnabled) {
    sessionRoutes(config.sessionHandler)
  }

  if (isEmailPasswordEnabled) {
    emailPasswordRoutes(config.emailPasswordHandler)
  }

  if (isThirdPartyEnabled) {
    thirdPartyRoutes(config.thirdPartyHandler)
  }

  if (isEmailVerificationEnabled) {
    emailVerificationRoutes(config.emailVerificationHandler)
  }

  if (isPasswordlessEnabled) {
    passwordlessRoutes(config.passwordlessHandler)
  }

  if (isTotpEnabled) {
    totpRoutes(config.totpHandler)
  }

  if (isMultiFactorAuthEnabled) {
    multiFactorRoutes(config.mfaHandler)
  }
}

val SuperTokensAttributeKey = AttributeKey<SuperTokens>("SuperTokens")

val ApplicationCall.superTokens: SuperTokens
  get() = application.attributes[SuperTokensAttributeKey]
val PipelineContext<Unit, ApplicationCall>.superTokens: SuperTokens
  get() = context.attributes[SuperTokensAttributeKey]
val Route.superTokens: SuperTokens
  get() = application.attributes[SuperTokensAttributeKey]
val RoutingContext.superTokens: SuperTokens
  get() = call.application.attributes[SuperTokensAttributeKey]
