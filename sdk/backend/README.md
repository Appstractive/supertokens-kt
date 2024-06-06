# SuperTokens Kotlin Backend SDK

## Installation

Gradle:
```
implementation("com.appstractive:supertokens-sdk-backend:1.4.1")
```

## Usage

```
val mailService = SmtpEmailService(
    SmtpConfig(
        host = "localhost",
        port = "555",
        password = "smtpPassword",
        fromEmail = "smtpUser",
        fromName = "SuperTokens Test",
    )
)

val superTokens = superTokens(
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
```