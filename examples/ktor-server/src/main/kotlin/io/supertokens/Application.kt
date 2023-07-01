package io.supertokens

import com.supertokens.ktor.SuperTokens
import com.supertokens.ktor.plugins.AuthenticatedUser
import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.plugins.requirePrincipal
import com.supertokens.ktor.plugins.superTokens
import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.session.CustomJwtData
import com.supertokens.sdk.recipes.session.Sessions
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
        superTokens = superTokens(
            connectionURI = "https://try.supertokens.io",
            appConfig = AppConfig(
                name = "Ktor Example Server",
            ),
        ) {
            recipe(EmailPassword) {

            }
            recipe(Sessions) {
                customJwtData { _ , user ->
                    buildMap {
                        set("email", user.email)
                    }
                }
            }
        }

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
                val user =  call.requirePrincipal<AuthenticatedUser>(SuperTokensAuth)

                call.respond(PrivateUserResponse(
                    id = user.id,
                ))
            }
        }
    }

}