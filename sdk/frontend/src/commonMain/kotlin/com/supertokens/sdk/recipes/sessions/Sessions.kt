package com.supertokens.sdk.recipes.sessions

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.HEADER_ACCESS_TOKEN
import com.supertokens.sdk.common.HEADER_REFRESH_TOKEN
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.Sender
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode

fun SuperTokensClient.tokenHeaderInterceptor(): suspend Sender.(HttpRequestBuilder) -> HttpClientCall = { request ->
    execute(request).also {
        if(it.response.status == HttpStatusCode.OK) {
            it.response.headers[HEADER_ACCESS_TOKEN]?.let { token ->
                if(token.isNotBlank()) {
                    tokensUseCase.updateAccessToken(token)
                }
                else {
                    tokensUseCase.clearAccessToken()
                }
            }

            it.response.headers[HEADER_REFRESH_TOKEN]?.let { token ->
                if(token.isNotBlank()) {
                    tokensUseCase.updateRefreshToken(token)
                }
                else {
                    tokensUseCase.clearRefreshToken()
                }
            }
        }
    }
}