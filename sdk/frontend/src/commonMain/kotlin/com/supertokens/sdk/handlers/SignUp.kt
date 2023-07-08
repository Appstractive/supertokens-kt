package com.supertokens.sdk.handlers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.FormFieldError

interface SignupProviderConfig

interface SignUpProvider<C: SignupProviderConfig> {

    suspend fun signUp(superTokensClient: SuperTokensClient, configure: (C.() -> Unit)): User

}

suspend fun <C, Provider : SignUpProvider<C>> SuperTokensClient.signUpWith(
    provider: Provider,
    config: (C.() -> Unit)
): User {
    return provider.signUp(this, config)
}

class FormFieldException(val errors: List<FormFieldError>): SuperTokensStatusException(SuperTokensStatus.FormFieldError)