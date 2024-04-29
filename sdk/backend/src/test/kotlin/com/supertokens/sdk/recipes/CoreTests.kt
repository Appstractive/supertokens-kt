package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.createJwt
import com.supertokens.sdk.core.deleteUser
import com.supertokens.sdk.core.getUserById
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.core.getUsersByPhoneNumber
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.emailpassword.EmailPassword
import com.supertokens.sdk.recipes.emailpassword.emailPasswordSignUp
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.consumePasswordlessUserInputCode
import com.supertokens.sdk.recipes.passwordless.createPasswordlessPhoneNumberCode
import io.fusionauth.jwt.JWTDecoder
import io.fusionauth.jwt.Verifier
import io.fusionauth.jwt.domain.Algorithm
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreTests : BaseTest() {

    override fun SuperTokensConfig.configure() {
        recipe(EmailPassword)
        recipe(Passwordless)
    }

    @Test
    fun testGetUserById() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER, null).first()

        val getResponse = superTokens.getUserById(user.id)

        assertEquals(user.id, getResponse.id)
    }

    @Test
    fun testGetUserByMail() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER, null).first()

        assertEquals(TEST_USER, user.email)
    }

    @Test
    fun testGetUserByPhoneNumber() = runBlocking {
        val code = superTokens.createPasswordlessPhoneNumberCode("+491601234567")
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.consumePasswordlessUserInputCode(
            code.preAuthSessionId,
            code.deviceId,
            code.userInputCode
        )
        val user = superTokens.getUsersByPhoneNumber("+491601234567", null).first()

        assertEquals("+491601234567", user.phoneNumber)
    }

    @Test
    fun testDeleteUser() = runBlocking {
        val email = "${Instant.now().epochSecond}@test.de"
        val user = superTokens.emailPasswordSignUp(email, TEST_PASSWORD)

        val response = superTokens.deleteUser(user.id)
        assertEquals(SuperTokensStatus.OK, response)
    }

    @Test
    fun testCreateJwt() = runBlocking {
        val jwt = superTokens.createJwt(
            issuer = "Test",
            validityInSeconds = 60,
            payload = jwtData,
        )

        val decoded = JWTDecoder().decode(jwt, object : Verifier {
            override fun canVerify(algorithm: Algorithm?) = true
            override fun verify(
                algorithm: Algorithm?,
                message: ByteArray?,
                signature: ByteArray?
            ) {
            }
        })

        assertEquals("Test", decoded.issuer)
        for (data in jwtData) {
            assertTrue(decoded.allClaims.contains(data.key))
        }
    }

    private enum class TestEnum {
        VALUE1,
        VALUE2,
        VALUE3,
    }

    @Serializable
    private data class TestClass(
        val bool: Boolean,
        val number: Double,
        val string: String,
        val enum: TestEnum,
    )

    companion object {

        private val jsonEncoder = Json { encodeDefaults = true }

        val jwtData: Map<String, Any?> = mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to listOf<Any?>(
                null,
                false,
                789.123,
                "World",
                TestEnum.VALUE2,
            ),
            "object" to mapOf<String, Any?>(
                "null" to null,
                "bool" to true,
                "number" to 42,
                "string" to "Hello",
                "enum" to TestEnum.VALUE3,
            ),
            "complex" to jsonEncoder.encodeToJsonElement(
                TestClass(
                    bool = true,
                    number = 169.270,
                    string = "!",
                    enum = TestEnum.VALUE2,
                )
            ),
        )
    }

}