package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.totp.Totp
import com.supertokens.sdk.recipes.totp.TotpRecipe
import com.supertokens.sdk.recipes.totp.addTotpDevice
import com.supertokens.sdk.recipes.totp.changeTotpDeviceName
import com.supertokens.sdk.recipes.totp.getTotpDevices
import com.supertokens.sdk.recipes.totp.removeTotpDevice
import com.supertokens.sdk.recipes.totp.verifyTotpCode
import com.supertokens.sdk.recipes.totp.verifyTotpDevice
import com.supertokens.sdk.superTokens
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Ignore("Requires License")
class TotpTests {

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
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
    fun testRenameDevice() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        var response = superTokens.changeTotpDeviceName(user.id, TEST_TOTP_DEVICE_NAME, "Test TOTP Device 2")
        assertEquals(SuperTokensStatus.OK, response)

        response = superTokens.changeTotpDeviceName(user.id, "Test TOTP Device 2", TEST_TOTP_DEVICE_NAME)
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

        val response = superTokens.verifyTotpDevice(user.id, TEST_TOTP_DEVICE_NAME, generator.generate())
        assertTrue(response)
    }

    @Test
    fun testVerifyCode() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()
        superTokens.removeTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)

        val secret = superTokens.addTotpDevice(user.id, TEST_TOTP_DEVICE_NAME)
        val generator = getTotpGenerator(secret)

        val response = superTokens.verifyTotpCode(user.id, generator.generate())
        assertTrue(response)
    }

    private fun getTotpGenerator(secret: String): TimeBasedOneTimePasswordGenerator {
        val config = TimeBasedOneTimePasswordConfig(
            codeDigits = 6,
            hmacAlgorithm = HmacAlgorithm.SHA1,
            timeStep = 30,
            timeStepUnit = TimeUnit.SECONDS,
        )
        return TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
    }

    companion object {
        const val TEST_USER = "test@test.de"
        const val TEST_TOTP_DEVICE_NAME = "Test TOTP Device"
    }

}