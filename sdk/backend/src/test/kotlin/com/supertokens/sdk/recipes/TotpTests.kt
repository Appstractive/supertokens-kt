package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUserByEMail
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
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Ignore("Requires SUperTokens License")
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
        val user = superTokens.getUserByEMail("test@test.de")

        val secret = superTokens.addTotpDevice(user.id, "Test TOTP Device")
        assertTrue(secret.isNotEmpty())

        val devices = superTokens.getTotpDevices(user.id)

        val device = assertNotNull(devices.firstOrNull { it.name == "Test TOTP Device" })
    }

    @Test
    fun testRenameDevice() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        val secret = superTokens.addTotpDevice(user.id, "Test TOTP Device")

        var response = superTokens.changeTotpDeviceName(user.id, "Test TOTP Device", "Test TOTP Device 2")
        assertEquals(SuperTokensStatus.OK, response)

        response = superTokens.changeTotpDeviceName(user.id, "Test TOTP Device 2", "Test TOTP Device")
    }

    @Test
    fun testRemoveDevice() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        val secret = superTokens.addTotpDevice(user.id, "Test TOTP Device")

        val response = superTokens.removeTotpDevice(user.id, "Test TOTP Device")
        assertTrue(response)

        val devices = superTokens.getTotpDevices(user.id)

        val device = assertNull(devices.firstOrNull { it.name == "Test TOTP Device" })
    }

    @Test
    fun testVerifyDevice() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        val secret = superTokens.addTotpDevice(user.id, "Test TOTP Device")

        val response = superTokens.verifyTotpDevice(user.id, "Test TOTP Device", "123456")
        assertTrue(response)
    }

    @Test
    fun testVerifyCode() = runBlocking {
        val user = superTokens.getUserByEMail("test@test.de")

        val secret = superTokens.addTotpDevice(user.id, "Test TOTP Device")

        val response = superTokens.verifyTotpCode(user.id, "123456")
        assertTrue(response)
    }

}