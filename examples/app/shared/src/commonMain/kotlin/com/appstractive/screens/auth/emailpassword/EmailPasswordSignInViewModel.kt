package com.appstractive.screens.auth.emailpassword

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.appstractive.ViewModel
import com.appstractive.dependencies
import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class EmailPasswordSignInViewModel(
    private val client: SuperTokensClient = dependencies.superTokensClient,
): ViewModel() {

    val email = mutableStateOf(TextFieldValue("test@test.de"))
    val password = mutableStateOf(TextFieldValue("a1234567"))

    fun emailChanged(value: TextFieldValue) {
        email.value = value
    }

    fun passwordChanged(value: TextFieldValue) {
        password.value = value
    }

    suspend fun submit(): Boolean {
        return withContext(Dispatchers.IO) {
            isLoading.value = true
            val result = runCatching {
                client.signInWith(EmailPassword) {
                    email = this@EmailPasswordSignInViewModel.email.value.text
                    password = this@EmailPasswordSignInViewModel.password.value.text
                }
            }

            if(result.isFailure) {
                error.value = result.exceptionOrNull()?.message
            }

            isLoading.value = false

            result.isSuccess
        }
    }

}