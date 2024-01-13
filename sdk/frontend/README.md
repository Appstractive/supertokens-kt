# SuperTokens [KTOR](https://github.com/ktorio/ktor) Server Plugin

## Installation

Gradle:
```
implementation("com.appstractive:supertokens-sdk-frontend:1.3.1")
```

## Usage

### Client creation
```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(EmailPassword)
    
    recipe(Session) {
        tokensRepository = TokensRepositoryMemory()
    }
    
    recipe(ThirdParty) {
        provider(Google) {
            redirectUri = "localhost"
        }
    }
}
```

### EmailPassword

#### SignUp

```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(EmailPassword)
}

val user = client.signUpWith(EmailPassword) {
    email = "test@test.de"
    password = "a1234567"
}
```

#### SignIn

```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(EmailPassword)
}

val user = client.signInWith(EmailPassword) {
    email = "test@test.de"
    password = "a1234567"
}
```

### Passwordless

```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(Passwordless)
}

val data = client.signUpWith(Passwordless) {
    email = "test@test.de"
}

// wait email

val userData = client.signInWith(PasswordlessInputCode) {
    preAuthSessionId = data.preAuthSessionId
    deviceId = data.deviceId
    userInputCode = "12345"
}
```

### ThirdParty

```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(ThirdParty) {
        provider(Google) {
            redirectUri = "localhost"
        }
    }
}

val signInData = client.signInWith(Google.Tokens) {
    accessToken = "123456"
    idToken = "123456"
}
```

### Authenticated API Calls

```
val client = superTokensClient("https://auth.appstractive.com") {
    recipe(EmailPassword)
    recipe(Session) {
        tokensRepository = TokensRepositoryMemory()
    }
}

val user = client.signInWith(EmailPassword) {
    email = "test@test.de"
    password = "a1234567"
}

val response = client.apiClient.get("/private")
```