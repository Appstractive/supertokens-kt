package com.appstractive.screens.home

import androidx.compose.runtime.mutableStateOf
import com.appstractive.ViewModel
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signOut
import com.supertokens.sdk.repositories.user.getUserId
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    scope: CoroutineScope,
    private val client: SuperTokensClient = dependencies.superTokensClient,
): ViewModel(scope) {

    val userId = mutableStateOf("")
    val privateResponse = mutableStateOf<String?>(null)

    init {
        scope.launch {
            userId.value = client.getUserId() ?: "UNKNOWN"

            val response = client.apiClient.get("/private")
            privateResponse.value = response.bodyAsText()
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            runCatching {
                client.signOut()
            }
        }
    }

}