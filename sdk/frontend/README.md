# SuperTokens Kotlin Multiplatform Frontend SDK

## Installation

Gradle:

```
implementation("com.appstractive:supertokens-sdk-frontend:1.4.1")
```

## Usage

### Client creation

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
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

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
    recipe(EmailPassword)
}

val user = client.signUpWith(EmailPassword) {
    email = "test@test.de"
    password = "a1234567"
}
```

#### SignIn

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
    recipe(EmailPassword)
}

val user = client.signInWith(EmailPassword) {
    email = "test@test.de"
    password = "a1234567"
}
```

### Passwordless

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
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

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
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

``` kotlin
val client = superTokensClient("https://auth.appstractive.cloud") {
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