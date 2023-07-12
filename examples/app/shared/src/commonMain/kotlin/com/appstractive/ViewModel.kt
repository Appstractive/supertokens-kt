package com.appstractive

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.CoroutineScope

abstract class ViewModel(
    internal val scope: CoroutineScope,
): ScreenModel {

    val isLoading = mutableStateOf(false)

}