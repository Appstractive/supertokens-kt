package com.supertokens.sdk.recipes.passwordless

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequest
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequest
import com.supertokens.sdk.common.responses.SignInUpResponse
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.handlers.SignUpProvider
import com.supertokens.sdk.handlers.SignupProviderConfig
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

data class PasswordlessSignUpData(
    val deviceId: String,
    val preAuthSessionId: String,
    val flowType: PasswordlessMode,
)

data class PasswordlessSignInData(
    val createdNewUser: Boolean,
    val user: User,
)

object Passwordless : SignInProvider<Passwordless.SignInConfig, PasswordlessSignInData>, SignUpProvider<Passwordless.SignUpConfig, PasswordlessSignUpData> {

    data class SignUpConfig(
        var email: String? = null,
        var phoneNumber: String? = null
    ) : SignupProviderConfig

    data class SignInConfig(
        var preAuthSessionId: String = "",
        var linkCode: String? = null,
        var deviceId: String? = null,
        var userInputCode: String? = null,
    ) : SignInProviderConfig

    override suspend fun signUp(superTokensClient: SuperTokensClient, configure: SignUpConfig.() -> Unit): PasswordlessSignUpData {
        val config = SignUpConfig().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.Passwordless.SIGNUP_CODE) {
            setBody(
                StartPasswordlessSignInUpRequest(
                    email = config.email,
                    phoneNumber = config.phoneNumber,
                )
            )
        }

        val body = response.body<StartPasswordlessSignInUpResponse>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                PasswordlessSignUpData(
                    deviceId = checkNotNull(body.deviceId),
                    preAuthSessionId = checkNotNull(body.preAuthSessionId),
                    flowType = checkNotNull(body.flowType),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: SignInConfig.() -> Unit): PasswordlessSignInData {
        val config = SignInConfig().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.Passwordless.SIGNUP_CODE_CONSUME) {
            setBody(
                ConsumePasswordlessCodeRequest(
                    preAuthSessionId = config.preAuthSessionId,
                    linkCode = config.linkCode,
                    deviceId = config.deviceId,
                    userInputCode = config.userInputCode,
                )
            )
        }

        val body = response.body<SignInUpResponse>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                PasswordlessSignInData(
                    user = checkNotNull(body.user),
                    createdNewUser = checkNotNull(body.createdNewUser),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

}