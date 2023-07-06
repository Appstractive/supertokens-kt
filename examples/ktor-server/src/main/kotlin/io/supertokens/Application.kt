package io.supertokens

import com.supertokens.ktor.SuperTokens
import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.plugins.superTokens
import com.supertokens.ktor.plugins.withPermission
import com.supertokens.ktor.plugins.withRole
import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.ServerConfig
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
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
import com.supertokens.sdk.superTokens
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.cio.*
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
                frontends = listOf(
                    ServerConfig(),
                    ServerConfig(
                        scheme = "my-app",
                        host = "callbacks",
                        path = "",
                    ),
                ),
            ),
        ) {
            recipe(EmailPassword) {
                emailService = mailService
            }
            recipe(Sessions) {
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

            recipe(Roles) {

            }
        }

        emailPasswordHandler = CustomEmailPasswordHandler()

        jwtVerification {
            withAudience("localhost")
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