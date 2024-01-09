package com.supertokens.sdk.recipes.passwordless

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequestDTO
import com.supertokens.sdk.common.requests.StartPasswordlessSignInUpRequestDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.responses.StartPasswordlessSignInUpResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.handlers.SignUpProvider
import com.supertokens.sdk.handlers.SignUpProviderConfig
import com.supertokens.sdk.models.SignInData
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

data class PasswordlessSignUpData(
    val deviceId: String,
    val preAuthSessionId: String,
    val flowType: PasswordlessMode,
)

object Passwordless : SignUpProvider<Passwordless.SignUpConfig, PasswordlessSignUpData> {

    data class SignUpConfig(
        var email: String? = null,
        var phoneNumber: String? = null
    ) : SignUpProviderConfig

    override suspend fun signUp(superTokensClient: SuperTokensClient, configure: SignUpConfig.() -> Unit): PasswordlessSignUpData {
        val config = SignUpConfig().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.Passwordless.SIGNUP_CODE) {
            setBody(
                StartPasswordlessSignInUpRequestDTO(
                    email = config.email,
                    phoneNumber = config.phoneNumber,
                )
            )
        }

        val body = response.body<StartPasswordlessSignInUpResponseDTO>()

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

}

object PasswordlessLinkCode : SignInProvider<PasswordlessLinkCode.SignInConfig, SignInData> {

    data class SignInConfig(
        var preAuthSessionId: String = "",
        var linkCode: String = "",
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: SignInConfig.() -> Unit): SignInData {
        val config = SignInConfig().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.Passwordless.SIGNUP_CODE_CONSUME) {
            setBody(
                ConsumePasswordlessCodeRequestDTO(
                    preAuthSessionId = config.preAuthSessionId,
                    linkCode = config.linkCode,
                )
            )
        }

        val body = response.body<SignInUpResponseDTO>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                SignInData(
                    user = checkNotNull(body.user),
                    createdNewUser = checkNotNull(body.createdNewUser),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }

}

object PasswordlessInputCode : SignInProvider<PasswordlessInputCode.SignInConfig, SignInData> {

    data class SignInConfig(
        var preAuthSessionId: String = "",
        var deviceId: String = "",
        var userInputCode: String = "",
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: SignInConfig.() -> Unit): SignInData {
        val config = SignInConfig().apply(configure)

        val response = superTokensClient.apiClient.post(Routes.Passwordless.SIGNUP_CODE_CONSUME) {
            setBody(
                ConsumePasswordlessCodeRequestDTO(
                    preAuthSessionId = config.preAuthSessionId,
                    deviceId = config.deviceId,
                    userInputCode = config.userInputCode,
                )
            )
        }

        val body = response.body<SignInUpResponseDTO>()

        return when(val status = body.status.toStatus()) {
            SuperTokensStatus.OK -> {
                SignInData(
                    user = checkNotNull(body.user),
                    createdNewUser = checkNotNull(body.createdNewUser),
                )
            }
            else -> throw SuperTokensStatusException(status)
        }
    }


}