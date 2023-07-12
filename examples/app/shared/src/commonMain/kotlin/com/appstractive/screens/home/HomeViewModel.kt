package com.appstractive.screens.home

import androidx.compose.runtime.mutableStateOf
import com.appstractive.ViewModel
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signOut
import com.supertokens.sdk.repositories.user.getUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    scope: CoroutineScope,
    private val apiClient: SuperTokensClient = dependencies.apiClient,
): ViewModel(scope) {

    val userId = mutableStateOf("")

    init {
        scope.launch {
            userId.value = apiClient.getUserId() ?: "UNKNOWN"
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            runCatching {
                apiClient.signOut()
            }
        }
    }

}