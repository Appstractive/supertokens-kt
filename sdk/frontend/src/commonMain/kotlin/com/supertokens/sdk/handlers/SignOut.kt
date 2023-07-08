package com.supertokens.sdk.handlers

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import io.ktor.client.request.post

suspend fun SuperTokensClient.signOut() {
    val response = apiClient.post(Routes.Session.SIGN_OUT)
    tokensRepository.clearTokens()
}