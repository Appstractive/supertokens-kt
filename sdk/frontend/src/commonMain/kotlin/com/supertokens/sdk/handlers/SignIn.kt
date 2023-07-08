package com.supertokens.sdk.handlers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.models.User

interface SignInProviderConfig

interface SignInProvider<C: SignInProviderConfig> {

    suspend fun signIn(superTokensClient: SuperTokensClient, configure: (C.() -> Unit)): User

}

suspend fun <C, Provider : SignInProvider<C>> SuperTokensClient.signInWith(
    provider: Provider,
    config: (C.() -> Unit)
): User {
    return provider.signIn(this, config)
}