package com.appstractive

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.appstractive.screens.splash.SplashScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(SplashScreen())
    }
}