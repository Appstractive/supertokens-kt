package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.passwordless.Passwordless
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import com.supertokens.sdk.recipes.passwordless.consumePasswordlessLinkCode
import com.supertokens.sdk.recipes.passwordless.consumePasswordlessUserInputCode
import com.supertokens.sdk.recipes.passwordless.createPasswordlessEmailCode
import com.supertokens.sdk.recipes.passwordless.createPasswordlessPhoneNumberCode
import com.supertokens.sdk.recipes.passwordless.getPasswordlessCodesByDeviceId
import com.supertokens.sdk.recipes.passwordless.getPasswordlessCodesByEmail
import com.supertokens.sdk.recipes.passwordless.getPasswordlessCodesByPhoneNumber
import com.supertokens.sdk.recipes.passwordless.getPasswordlessCodesByPreAuthSessionId
import com.supertokens.sdk.recipes.passwordless.recreatePasswordlessCode
import com.supertokens.sdk.recipes.passwordless.revokePasswordlessCode
import com.supertokens.sdk.recipes.passwordless.revokePasswordlessEmailCodes
import com.supertokens.sdk.recipes.passwordless.revokePasswordlessPhoneNumberCodes
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PasswordlessRecipeTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
        recipe(Passwordless) {

        }
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<PasswordlessRecipe>()
    }

    @Test
    fun testCreateEmailCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())
    }

    @Test
    fun testCreatePhoneNumberCode() = runBlocking {
        val code = superTokens.createPasswordlessPhoneNumberCode(TEST_PHONE_NUMBER)
        assertTrue(code.codeId.isNotEmpty())
    }

    @Test
    fun testRecreateCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val newCode = superTokens.recreatePasswordlessCode(code.deviceId)
        assertTrue(newCode.codeId.isNotEmpty())
    }

    @Test
    fun testConsumeLinkCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.consumePasswordlessLinkCode(code.preAuthSessionId, code.linkCode)
        assertEquals(TEST_USER, response.user.email)
    }

    @Test
    fun testConsumeUserInputCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.consumePasswordlessUserInputCode(code.preAuthSessionId, code.deviceId, code.userInputCode)
        assertEquals(TEST_USER, response.user.email)
    }

    @Test
    fun testConsumeCodeFailed() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val exception = assertThrows(SuperTokensStatusException::class.java) {
            runBlocking {
                superTokens.consumePasswordlessUserInputCode(code.preAuthSessionId, code.deviceId, "wrong code")
            }
        }

        assertEquals(SuperTokensStatus.PasswordlessIncorrectCodeError, exception.status)
    }

    @Test
    fun testRevokeCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.revokePasswordlessCode(code.codeId)
        assertEquals(SuperTokensStatus.OK, response)
    }

    @Test
    fun testRevokeAllCode() = runBlocking {
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val response = superTokens.revokePasswordlessEmailCodes(TEST_USER)
        assertEquals(SuperTokensStatus.OK, response)
    }

    @Test
    fun testGetCodesByEmail() = runBlocking {
        superTokens.revokePasswordlessEmailCodes(TEST_USER)
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val codes = superTokens.getPasswordlessCodesByEmail(TEST_USER)
        assertTrue(codes.isNotEmpty())
        assertNotNull(codes.first().codes.firstOrNull {
            it.codeId == code.codeId
        })

        assertEquals(TEST_USER, codes.first().email)
    }

    @Test
    fun testGetCodesByPhoneNumber() = runBlocking {
        superTokens.revokePasswordlessPhoneNumberCodes(TEST_PHONE_NUMBER)
        val code = superTokens.createPasswordlessPhoneNumberCode(TEST_PHONE_NUMBER)
        assertTrue(code.codeId.isNotEmpty())

        val codes = superTokens.getPasswordlessCodesByPhoneNumber(TEST_PHONE_NUMBER)
        assertTrue(codes.isNotEmpty())
        assertNotNull(codes.first().codes.firstOrNull {
            it.codeId == code.codeId
        })

        assertEquals(TEST_PHONE_NUMBER, codes.first().phoneNumber)
    }

    @Test
    fun testGetCodesByDeviceId() = runBlocking {
        superTokens.revokePasswordlessEmailCodes(TEST_USER)
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val codes = superTokens.getPasswordlessCodesByDeviceId(code.deviceId)
        assertTrue(codes.isNotEmpty())
        assertNotNull(codes.first().codes.firstOrNull {
            it.codeId == code.codeId
        })

        assertEquals(TEST_USER, codes.first().email)
    }

    @Test
    fun testGetCodesByPreAuthSessionId() = runBlocking {
        superTokens.revokePasswordlessEmailCodes(TEST_USER)
        val code = superTokens.createPasswordlessEmailCode(TEST_USER)
        assertTrue(code.codeId.isNotEmpty())

        val codes = superTokens.getPasswordlessCodesByPreAuthSessionId(code.preAuthSessionId)
        assertTrue(codes.isNotEmpty())
        assertNotNull(codes.first().codes.firstOrNull {
            it.codeId == code.codeId
        })

        assertEquals(TEST_USER, codes.first().email)
    }

    companion object {
        const val TEST_USER = "test@test.de"
        const val TEST_PHONE_NUMBER = "+491601234567"
    }

}