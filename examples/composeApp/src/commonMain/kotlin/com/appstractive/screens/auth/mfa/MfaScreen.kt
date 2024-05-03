package com.appstractive.screens.auth.mfa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.appstractive.LocalDependencies
import com.appstractive.screens.auth.AuthScreen
import com.appstractive.util.CommonParcelize
import com.appstractive.util.rememberApiCallController
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.models.MultiFactorAuthStatus
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuthRecipe
import com.supertokens.sdk.recipes.sessions.signOut

@CommonParcelize
data object MfaScreen: Screen {
    data class State(
        val mfaStatus: MultiFactorAuthStatus?,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object Refresh : Event
        data object GotoTotp : Event
        data object GotoEmailOtp : Event
        data object Cancel : Event
    }
}

@Composable
fun MfaScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): MfaScreen.State {
    val apiCallController = rememberApiCallController()
    var mfaStatus: MultiFactorAuthStatus? by rememberRetained {
        mutableStateOf(null)
    }

    LaunchedEffect(superTokensClient) {
        apiCallController.call {
            mfaStatus = superTokensClient.getRecipe<MultiFactorAuthRecipe>().checkMfaStatus()
        }
    }

    return MfaScreen.State(
        mfaStatus = mfaStatus,
    ) {
        when (it) {
            MfaScreen.Event.Cancel -> apiCallController.call {
                superTokensClient.signOut()
                navigator.resetRoot(AuthScreen)
            }
            MfaScreen.Event.GotoEmailOtp -> navigator.goTo(EmailOtpScreen)
            MfaScreen.Event.GotoTotp -> navigator.goTo(TotpScreen)
            MfaScreen.Event.Refresh -> apiCallController.call {
                mfaStatus = superTokensClient.getRecipe<MultiFactorAuthRecipe>().checkMfaStatus()
            }
        }
    }
}

class MfaScreenPresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is MfaScreen -> presenterOf { MfaScreenPresenter(navigator) }
            is EmailOtpScreen -> presenterOf { EmailOtpScreenPresenter(navigator) }
            is TotpScreen -> presenterOf { TotpScreenPresenter(navigator) }
            else -> null
        }
    }
}
