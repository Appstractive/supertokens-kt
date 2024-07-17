package com.appstractive

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.runtime.Navigator

expect fun getPlatformDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true,
    decorFitsSystemWindows: Boolean = true,
    usePlatformInsets: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
): DialogProperties

@Composable internal expect fun getNavigator(backstack: BackStack<out BackStack.Record>): Navigator
