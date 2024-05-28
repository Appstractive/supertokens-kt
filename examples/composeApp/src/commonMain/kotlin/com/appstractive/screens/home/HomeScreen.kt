package com.appstractive.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.supertokens.sdk.common.claims.AccessTokenClaims
import com.supertokens.sdk.recipes.sessions.signOut
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

@CommonParcelize
data object HomeScreen : Screen {

    data class State(
        val claims: AccessTokenClaims?,
        val privateApiResponse: String?,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object Logout : Event
    }

}

@Composable
fun HomeScreenPresenter(
    navigator: Navigator,
    superTokensClient: SuperTokensClient = LocalDependencies.current.superTokensClient,
): HomeScreen.State {
    val apiCallController = rememberApiCallController()
    var privateApiResponse: String? by rememberRetained() {
        mutableStateOf(null)
    }
    val claims by superTokensClient.claimsRepository.claims.collectAsState()

    LaunchedEffect(superTokensClient) {
        apiCallController.call {
            val response = superTokensClient.apiClient.get("/private")
            privateApiResponse = response.bodyAsText()
        }
    }

    return HomeScreen.State(
        claims = claims,
        privateApiResponse = privateApiResponse,
    ) {
        when(it) {
            HomeScreen.Event.Logout -> apiCallController.call {
                superTokensClient.signOut()
                navigator.resetRoot(AuthScreen)
            }
        }
    }
}

class HomeScreenPresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is HomeScreen -> presenterOf { HomeScreenPresenter(navigator) }
            else -> null
        }
    }
}
