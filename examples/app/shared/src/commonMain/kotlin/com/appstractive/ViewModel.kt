package com.appstractive

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel

abstract class ViewModel: ScreenModel {

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

}