package io.supertokens

import com.supertokens.ktor.SuperTokens
import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.plugins.superTokens
import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.FrontendConfig
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.ingredients.email.smtp.SmtpConfig
import com.supertokens.sdk.ingredients.email.smtp.SmtpEmailService
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailverification.EmailVerification
import com.supertokens.sdk.recipes.passwordless.Passwordless
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
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    install(SuperTokens) {

        val mailService = SmtpEmailService(
            SmtpConfig(
                host = "localhost",
                port = 1025,
                password = "",
                fromEmail = "test@example.com",
                fromName = "SuperTokens Test",
            )
        )

        superTokens = superTokens(
            connectionURI = "https://try.supertokens.io",
            appConfig = AppConfig(
                name = "Ktor Example Server",
                frontends = listOf(
                    FrontendConfig(),
                    FrontendConfig(
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
        }
    }

}