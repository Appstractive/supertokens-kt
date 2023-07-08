package com.supertokens.sdk

import com.supertokens.sdk.handlers.signInWith
import com.supertokens.sdk.handlers.signOut
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.repositories.tokens.TokensRepositoryMemory
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTests {

    private val client = superTokensClient("https://auth.appstractive.com") {
        tokensRepository = TokensRepositoryMemory()
    }

    @Serializable
    private data class PrivateResponse(
        val id: String,
    )

    @Test
    fun testPrivateApi() = runBlocking {
        val user = client.signInWith(EmailPassword) {
            email = "test@test.de"
            password = "a1234567"
        }

        assertEquals("test@test.de", user.email)

        val response = client.apiClient.get("/private") {

        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<PrivateResponse>()
        assertEquals(user.id, body.id)
    }

    @Test
    fun testSignOutPrivateApi() = runBlocking {
        val user = client.signInWith(EmailPassword) {
            email = "test@test.de"
            password = "a1234567"
        }

        assertEquals("test@test.de", user.email)

        var response = client.apiClient.get("/private")

        assertEquals(HttpStatusCode.OK, response.status)

        client.signOut()

        response = client.apiClient.get("/private")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

}