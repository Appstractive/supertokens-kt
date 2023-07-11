package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.ExistsResponse
import com.supertokens.sdk.common.toStatus
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.http.path

suspend fun SuperTokensClient.checkEmailExists(email: String): Boolean {
    val response = apiClient.get {
        url {
            path(Routes.EMAIL_EXISTS)
            parameters.append("email", email)
        }
    }

    val body = response.body<ExistsResponse>()

    return when(body.status) {
        SuperTokensStatus.OK.value -> body.exists
        else -> throw SuperTokensStatusException(body.status.toStatus())
    }
}

suspend fun SuperTokensClient.checkPhoneNumberExists(phoneNumber: String): Boolean {
    val response = apiClient.get {
        url {
            path(Routes.PHONE_NUMBER_EXISTS)
            parameters.append("phoneNumber", phoneNumber)
        }
    }

    val body = response.body<ExistsResponse>()

    return when(body.status) {
        SuperTokensStatus.OK.value -> body.exists
        else -> throw SuperTokensStatusException(body.status.toStatus())
    }
}