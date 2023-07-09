package com.supertokens.sdk.handlers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.FormFieldError

interface SignupProviderConfig

interface SignUpProvider<C: SignupProviderConfig, R> {

    suspend fun signUp(superTokensClient: SuperTokensClient, configure: (C.() -> Unit)): R

}

suspend fun <C, Provider : SignUpProvider<C, R>, R> SuperTokensClient.signUpWith(
    provider: Provider,
    config: (C.() -> Unit)
): R {
    return provider.signUp(this, config)
}

class FormFieldException(val errors: List<FormFieldError>): SuperTokensStatusException(SuperTokensStatus.FormFieldError)