package com.appstractive

import com.supertokens.ktor.SuperTokens
import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.plugins.superTokens
import com.supertokens.ktor.plugins.withPermission
import com.supertokens.ktor.plugins.withRole
import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.EndpointConfig
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_THIRD_PARTY
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.accountlinking.AccountLinking
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.recipes.accountlinking.ShouldDoAccountLinkingResult
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuth
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.roles.Roles
import com.supertokens.sdk.recipes.session.Sessions
import com.supertokens.sdk.recipes.thirdparty.ThirdParty
import com.supertokens.sdk.recipes.thirdparty.provider
import com.supertokens.sdk.recipes.thirdparty.providers.apple.Apple
import com.supertokens.sdk.recipes.thirdparty.providers.bitbucket.Bitbucket
import com.supertokens.sdk.recipes.thirdparty.providers.facebook.Facebook
import com.supertokens.sdk.recipes.thirdparty.providers.github.Github
import com.supertokens.sdk.recipes.thirdparty.providers.gitlab.GitLab
import com.supertokens.sdk.recipes.thirdparty.providers.google.Google
import com.supertokens.sdk.recipes.totp.Totp
import com.supertokens.sdk.superTokens
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.cio.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    val superTokensUrl = environment.config.propertyOrNull("supertokens.url")?.getString() ?: throw IllegalStateException("supertokens.url not configured")
    val superTokensIssuer = environment.config.propertyOrNull("supertokens.issuer")?.getString() ?: "localhost"
    val superTokensDomain = environment.config.propertyOrNull("supertokens.domain")?.getString() ?: "localhost"

    val frontendScheme = environment.config.propertyOrNull("supertokens.frontend.scheme")?.getString() ?: throw IllegalStateException("supertokens.frontend.scheme not configured")
    val frontendHost = environment.config.propertyOrNull("supertokens.frontend.host")?.getString() ?: throw IllegalStateException("supertokens.frontend.host not configured")
    val frontendPath = environment.config.propertyOrNull("supertokens.frontend.path")?.getString() ?: throw IllegalStateException("supertokens.frontend.path not configured")

    val apiScheme = environment.config.propertyOrNull("supertokens.api.scheme")?.getString() ?: throw IllegalStateException("supertokens.api.scheme not configured")
    val apiHost = environment.config.propertyOrNull("supertokens.api.host")?.getString() ?: throw IllegalStateException("supertokens.api.host not configured")
    val apiPath = environment.config.propertyOrNull("supertokens.api.path")?.getString() ?: throw IllegalStateException("supertokens.api.path not configured")

    val smtpUser = environment.config.propertyOrNull("smtp.user")?.getString() ?: throw IllegalStateException("smtp.user not configured")
    val smtpPassword = environment.config.propertyOrNull("smtp.password")?.getString() ?: throw IllegalStateException("smtp.password not configured")
    val smtpHost = environment.config.propertyOrNull("smtp.host")?.getString() ?: throw IllegalStateException("smtp.host not configured")
    val smtpPort = (environment.config.propertyOrNull("smtp.port")?.getString() ?: throw IllegalStateException("smtp.port not configured")).toInt()

    install(SuperTokens) {

        val mailService = SmtpEmailService(
            SmtpConfig(
                host = smtpHost,
                port = smtpPort,
                password = smtpPassword,
                fromEmail = smtpUser,
                fromName = "SuperTokens Test",
            )
        )

        superTokens = superTokens(
            connectionURI = superTokensUrl,
            appConfig = AppConfig(
                name = "Ktor Example Server",
                api = EndpointConfig(
                    scheme = apiScheme,
                    host = apiHost,
                    path = apiPath,
                ),
                frontends = listOf(
                    EndpointConfig(
                        scheme = frontendScheme,
                        host = frontendHost,
                        path = frontendPath,
                    ),
                    EndpointConfig(
                        scheme = "my-app",
                        host = "MyMobileApp",
                        path = "/callbacks",
                    ),
                ),
            ),
        ) {
            recipe(EmailPassword) {
                emailService = mailService
            }
            recipe(Sessions) {

                issuer = superTokensIssuer
                cookieDomain = superTokensDomain

                customJwtData { _, _ ->
                    mapOf(
                        "isExample" to true,
                    )
                }
            }

            recipe(ThirdParty) {

                provider(Github) {
                    clientId = "123456"
                    clientSecret = "abcdef"
                }

                provider(Facebook) {
                    clientId = "123456"
                    clientSecret = "abcdef"
                }

                provider(Bitbucket) {
                    clientId = "123456"
                    clientSecret = "abcdef"
                }

                provider(GitLab) {
                    clientId = "123456"
                    clientSecret = "abcdef"
                }

                provider(Google) {
                    clientId = "123456"
                    clientSecret = "abcdef"
                }

                provider(Apple) {
                    clientId = "123456"
                    keyId = "keyid"
                    privateKey = "-----BEGIN EC PRIVATE KEY-----\n" +
                            "MHcCAQEEIA15hjyZS/pWzMgI4SOlwKbbG4/c+3vQcFCfQaRhoFbzoAoGCCqGSM49\n" +
                            "AwEHoUQDQgAEkrYPIhuxDLQg8QKQnnto8JUFb13yWpY+venFhEzjhBwMgFl3oueT\n" +
                            "oQJf/l9sIYMIXc6gVnMg/lGEWv0ZANcYqg==\n" +
                            "-----END EC PRIVATE KEY-----"
                    teamId = "teamid"
                }
            }

            recipe(EmailVerification) {
                emailService = mailService
            }

            recipe(Passwordless) {
                emailService = mailService
                mode = PasswordlessMode.USER_INPUT_CODE_AND_MAGIC_LINK
            }

            recipe(Roles)

            recipe(AccountLinking) {
                shouldDoAutomaticAccountLinking = { _, _ ->
                    ShouldDoAccountLinkingResult(
                        shouldAutomaticallyLink = true
                    )
                }
            }
            recipe(Totp)
            recipe(MultiFactorAuth) {
                firstFactors = listOf(RECIPE_EMAIL_PASSWORD, RECIPE_THIRD_PARTY)
                getRequiredMultiFactors = { superTokens: SuperTokens, user: User, tenantId: String? ->
                    listOf(AuthFactor.AnyOf(AuthFactor.TOTP, AuthFactor.LINK_EMAIL, AuthFactor.OTP_EMAIL))
                }
            }
        }

        emailPasswordHandler = CustomEmailPasswordHandler(scope)

        jwtVerification {
            withAudience(frontendHost)
        }
    }

    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            encodeDefaults = true
        })
    }

    install(StatusPages) {
        superTokens(catchGeneralError = true)
    }

    install(CallLogging)

    routing {

        get("/public") {
            call.respondText("OK")
        }

        authenticate(SuperTokensAuth) {
            get("/private") {
                val user = call.requirePrincipal<AuthenticatedUser>()

                call.respond(
                    PrivateUserResponse(
                        id = user.id,
                    )
                )
            }

            withRole("admin") {
                get("/admin") {
                    val user = call.requirePrincipal<AuthenticatedUser>()

                    call.respond(
                        PrivateUserResponse(
                            id = user.id,
                        )
                    )
                }
            }

            withPermission("read:all") {
                get("/readall") {
                    val user = call.requirePrincipal<AuthenticatedUser>()

                    call.respond(
                        PrivateUserResponse(
                            id = user.id,
                        )
                    )
                }
            }
        }
    }

}