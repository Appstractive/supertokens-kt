package com.appstractive.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.appstractive.dependencies
import com.appstractive.screens.auth.AuthScreen
import com.appstractive.screens.home.HomeScreen
import com.supertokens.sdk.SuperTokensClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen(
    private val apiClient: SuperTokensClient = dependencies.apiClient,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val scope = rememberCoroutineScope()

        LaunchedEffect(scope) {
            scope.launch {
                delay(500)

                if(apiClient.isLoggedIn()) {
                    navigator.replace(HomeScreen)
                }
                else {
                    navigator.replace(AuthScreen)
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}