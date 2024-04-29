package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.totp.Totp
import com.supertokens.sdk.recipes.totp.TotpRecipe
import com.supertokens.sdk.recipes.totp.addTotpDevice
import com.supertokens.sdk.recipes.totp.changeTotpDeviceName
import com.supertokens.sdk.recipes.totp.getTotpDevices
import com.supertokens.sdk.recipes.totp.importTotpDevice
import com.supertokens.sdk.recipes.totp.removeTotpDevice
import com.supertokens.sdk.recipes.totp.verifyTotpCode
import com.supertokens.sdk.recipes.totp.verifyTotpDevice
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TotpTests : BaseTest() {

    override fun SuperTokensConfig.configure() {
        recipe(Totp)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<TotpRecipe>()
    }

    @Test
    fun testAddDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        assertTrue(secret.isNotEmpty())

        val devices = superTokens.getTotpDevices(user.id)

        val device = assertNotNull(devices.firstOrNull { it.name == TEST_TOTP_DEVICE_NAME })
    }

    @Test
    fun testImportDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val name = superTokens.importTotpDevice(user.id, TEST_TOTP_DEVICE_NAME, "rgret43634t3at")
        assertEquals(TEST_TOTP_DEVICE_NAME, name)

        val devices = superTokens.getTotpDevices(user.id)

        val device = assertNotNull(devices.firstOrNull { it.name == TEST_TOTP_DEVICE_NAME })
    }

    @Test
    fun testRenameDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        val devices = superTokens.getTotpDevices(userId = user.id)
        assertTrue(devices.isNotEmpty())

        var response =
            superTokens.changeTotpDeviceName(user.id, TEST_TOTP_DEVICE_NAME, "Test TOTP Device 2")
        assertEquals(SuperTokensStatus.OK, response)

        response =
            superTokens.changeTotpDeviceName(user.id, "Test TOTP Device 2", TEST_TOTP_DEVICE_NAME)
    }

    @Test
    fun testRemoveDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val response = superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        assertTrue(response)

        val devices = superTokens.getTotpDevices(user.id)

        val device = assertNull(devices.firstOrNull { it.name == TEST_TOTP_DEVICE_NAME })
    }

    @Test
    fun testVerifyDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        val generator = getTotpGenerator(secret)
        val totp = generator.generate()

        val response =
            superTokens.verifyTotpDevice(
                userId = user.id,
                deviceName = TEST_TOTP_DEVICE_NAME,
                totp = totp,
            )
        assertFalse(response)
    }

    @Test
    fun testVerifyCode() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        val generator = getTotpGenerator(secret)
        val totp = generator.generate()

        val response = superTokens.verifyTotpCode(
            userId = user.id,
            totp = totp,
            allowUnverifiedDevices = true,
        )
        assertTrue(response)
    }

    @Test
    fun testVerifyImportedCode() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = GoogleAuthenticator.createRandomSecret()

        superTokens.importTotpDevice(user.id, TEST_TOTP_DEVICE_NAME, secret)
        val generator = getTotpGenerator(secret)
        val totp = generator.generate()

        val response = superTokens.verifyTotpCode(
            userId = user.id,
            totp = totp,
            allowUnverifiedDevices = true,
        )
        assertTrue(response)
    }

    private fun getTotpGenerator(secret: String): GoogleAuthenticator {
        return GoogleAuthenticator(secret)
    }

    companion object {
        const val TEST_TOTP_DEVICE_NAME = "Test TOTP Device"
    }

}