package com.appstractive.util

import com.appstractive.screens.auth.AuthScreen
import com.appstractive.screens.auth.mfa.MfaScreen
import com.appstractive.screens.home.HomeScreen
import com.slack.circuit.runtime.screen.Screen
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.sessions.repositories.AuthState

fun SuperTokensClient.getHomeScreen(): Screen = when(val authState = authRepository.authState.value) {
    is AuthState.Authenticated -> {
        if(authState.multiFactorVerified) {
            HomeScreen
        }
        else {
            MfaScreen
        }
    }
    is AuthState.LoggedIn -> AuthScreen
    AuthState.Unauthenticated -> AuthScreen
}