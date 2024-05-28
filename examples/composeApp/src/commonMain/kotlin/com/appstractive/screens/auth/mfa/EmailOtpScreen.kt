package com.appstractive.screens.auth.mfa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.appstractive.LocalDependencies
import com.appstractive.screens.home.HomeScreen
import com.appstractive.util.CommonParcelize
import com.appstractive.util.rememberApiCallController
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.PasswordlessInputCode
import com.supertokens.sdk.recipes.passwordless.PasswordlessSignUpData

@CommonParcelize
data object EmailOtpScreen : Screen {
    data class State(
        val email: String?,
        val emailSend: Boolean,
        val eventSink: (Event) -> Unit = {},
    ): CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object SendEmail : Event
        data class ConfirmCode(
            val code: String,
        ) : Event
        data object GoBack : Event
    }
}

@Composable
fun EmailOtpScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): EmailOtpScreen.State {
    val apiCallController = rememberApiCallController()
    val claims by superTokensClient.claimsRepository.claims.collectAsState()
    var signUpData by remember { mutableStateOf<PasswordlessSignUpData?>(null) }

    return EmailOtpScreen.State(
        email = claims?.email,
        emailSend = signUpData != null,
    ) {
        when (it) {
            EmailOtpScreen.Event.GoBack -> navigator.pop()
            is EmailOtpScreen.Event.ConfirmCode -> apiCallController.call {
                superTokensClient.signInWith(PasswordlessInputCode) {
                    preAuthSessionId = signUpData?.preAuthSessionId
                    deviceId = signUpData?.deviceId
                    userInputCode = it.code
                }

                navigator.resetRoot(HomeScreen)
            }
            EmailOtpScreen.Event.SendEmail -> apiCallController.call {
                signUpData = superTokensClient.signUpWith(Passwordless) {
                    email = claims?.email
                }
            }
        }
    }
}
