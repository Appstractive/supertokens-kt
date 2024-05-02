package com.appstractive

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator

@OptIn(ExperimentalComposeUiApi::class)
actual fun getPlatformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean,
    usePlatformInsets: Boolean,
    scrimColor: Color
): DialogProperties  =
    DialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        usePlatformInsets = usePlatformInsets,
        scrimColor = scrimColor,
    )

@Composable
internal actual fun getNavigator(backstack: BackStack<out BackStack.Record>): Navigator {
    return rememberCircuitNavigator(backstack) {
    }
}