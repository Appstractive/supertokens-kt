package com.appstractive.screens.auth.passwordless

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.appstractive.LocalDependencies
import com.appstractive.screens.home.HomeScreen
import com.appstractive.util.CommonParcelize
import com.appstractive.util.getHomeScreen
import com.appstractive.util.rememberApiCallController
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signUpWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.PasswordlessInputCode

@CommonParcelize
data object PasswordlessScreen: Screen {
    sealed interface State : CircuitUiState {
        val eventSink: (Event) -> Unit

        data class SignIn(
            override val eventSink: (Event) -> Unit = {},
        ): State

        data class ConfirmCode(
            val deviceId: String,
            override val eventSink: (Event) -> Unit = {},
        ): State
    }

    sealed interface Event : CircuitUiEvent {
        data object GoBack : Event
        data class SignIn(val email: String) : Event
        data class ConfirmCode(val code: String) : Event
    }
}

@Composable
fun PasswordlessScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): PasswordlessScreen.State {
    val apiCallController = rememberApiCallController()
    var deviceId: String? by rememberRetained {
        mutableStateOf(null)
    }
    var preAuthSessionId: String? by rememberRetained {
        mutableStateOf(null)
    }

    val eventHandler: (PasswordlessScreen.Event) -> Unit = {
        when(it) {
            is PasswordlessScreen.Event.SignIn -> apiCallController.call {
                val result = superTokensClient.signUpWith(Passwordless) {
                    email = it.email
                }
                deviceId = result.deviceId
                preAuthSessionId = result.preAuthSessionId
            }

            is PasswordlessScreen.Event.ConfirmCode -> apiCallController.call {
                superTokensClient.signInWith(PasswordlessInputCode) {
                    this.preAuthSessionId = preAuthSessionId
                    this.deviceId = deviceId
                    this.userInputCode = it.code
                }
                navigator.resetRoot(superTokensClient.getHomeScreen())
            }

            PasswordlessScreen.Event.GoBack -> navigator.pop()

        }
    }

    return deviceId?.let {
        PasswordlessScreen.State.ConfirmCode(
            deviceId = it,
            eventSink = eventHandler,
        )
    } ?: PasswordlessScreen.State.SignIn(eventSink = eventHandler)
}
