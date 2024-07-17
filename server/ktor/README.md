# SuperTokens [KTOR](https://github.com/ktorio/ktor) Server Plugin

## Installation

Gradle

```
implementation("com.appstractive:supertokens-sdk-backend-ktor:1.4.1")
```

## Usage

``` kotlin
import com.supertokens.ktor.SuperTokens

fun Application.module() {
    val mailService = SmtpEmailService(
        SmtpConfig(
            host = "localhost",
            port = "555",
            password = "smtpPassword",
            fromEmail = "smtpUser",
            fromName = "SuperTokens Test",
        )
    )

    install(SuperTokens) {
        superTokens = superTokens(
            connectionURI = superTokensUrl,
            appConfig = AppConfig(
                name = "Ktor Example Server",
                api = ServerConfig(),
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

                issuer = "locahost"

                customJwtData { _, _ ->
                    mapOf(
                        "isExample" to true,
                    )
                }
            }
        }
    }
}
```