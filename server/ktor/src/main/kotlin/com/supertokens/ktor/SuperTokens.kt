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
import com.supertokens.ktor.recipes.emailverification.EmailVerificationHandler
import com.supertokens.ktor.recipes.emailverification.emailVerificationRoutes
import com.supertokens.ktor.recipes.passwordless.PasswordlessHandler
import com.supertokens.ktor.recipes.passwordless.passwordlessRoutes
import com.supertokens.ktor.recipes.session.SessionHandler
import com.supertokens.ktor.recipes.session.sessionRoutes
import com.supertokens.ktor.recipes.thirdparty.ThirdPartyHandler
import com.supertokens.ktor.recipes.thirdparty.thirdPartyRoutes
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import com.supertokens.sdk.recipes.emailverification.EmailVerificationRecipe
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.recipes.roles.RolesRecipe
import com.supertokens.sdk.recipes.session.SessionRecipe
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
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
import io.ktor.server.routing.application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json
import java.net.URL

typealias JwtVerification = Verification.() -> Unit

class SuperTokensConfig {

    // The SuperTokens instance to use (required)
    var superTokens: SuperTokens? = null

    // Handler for core APIs
    var coreHandler: CoreHandler = CoreHandler()

    // Handler for EmailPassword APIs
    var emailPasswordHandler: EmailPasswordHandler = EmailPasswordHandler()

    // Handler for Session APIs
    var sessionHandler: SessionHandler = SessionHandler()

    // Handler for ThirdParty APIs
    var thirdPartyHandler: ThirdPartyHandler = ThirdPartyHandler()

    // Handler for EmailVerification APIs
    var emailVerificationHandler: EmailVerificationHandler = EmailVerificationHandler()

    // Handler for Passwordless APIs
    var passwordlessHandler: PasswordlessHandler = PasswordlessHandler()

    // Allows you to perform additional validations on the JWT payload.
    var jwtValidator: suspend ApplicationCall.(JWTCredential) -> Principal? = TokenValidator


    internal var jwtVerification: JwtVerification? = null

    // used to verify a token format and signature
    fun jwtVerification(verify: JwtVerification) {
        jwtVerification = verify
    }

}

val SuperTokens = createApplicationPlugin(name = "SuperTokens", createConfiguration = ::SuperTokensConfig) {

    val config = pluginConfig
    val superTokens = config.superTokens ?: throw RuntimeException("SuperTokens SDK not configured")

    application.attributes.put(SuperTokensAttributeKey, superTokens)

    application.routing {

        route(superTokens.appConfig.api.path) {

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            coreRoutes(config.coreHandler)

            if (superTokens.hasRecipe<EmailPasswordRecipe>()) {
                emailPasswordRoutes(config.emailPasswordHandler)
            }

            if (superTokens.hasRecipe<SessionRecipe>()) {

                application.install(Authentication) {
                    jwt(name = SuperTokensAuth) {
                        validate(config.jwtValidator)

                        authHeader(authHeaderCookieWrapper)

                        verifier(UrlJwkProvider(URL(superTokens.jwksUrl)), superTokens.getRecipe<SessionRecipe>().issuer) {
                            config.jwtVerification?.invoke(this)
                        }
                    }

                    if(superTokens.hasRecipe<RolesRecipe>()) {
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

                sessionRoutes(config.sessionHandler)
            }

            if(superTokens.hasRecipe<ThirdPartyRecipe>()) {
                thirdPartyRoutes(config.thirdPartyHandler)
            }

            if(superTokens.hasRecipe<EmailVerificationRecipe>()) {
                emailVerificationRoutes(config.emailVerificationHandler)
            }

            if(superTokens.hasRecipe<PasswordlessRecipe>()) {
                passwordlessRoutes(config.passwordlessHandler)
            }
        }
    }
}

val SuperTokensAttributeKey = AttributeKey<SuperTokens>("SuperTokens")

val ApplicationCall.superTokens: SuperTokens get() = application.attributes[SuperTokensAttributeKey]
val PipelineContext<Unit, ApplicationCall>.superTokens: SuperTokens get() = application.attributes[SuperTokensAttributeKey]
val Route.superTokens: SuperTokens get() = application.attributes[SuperTokensAttributeKey]