package com.supertokens.sdk.recipes.totp

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.session.SessionRecipe
import com.supertokens.sdk.recipes.totp.models.TotpDevice
import com.supertokens.sdk.recipes.totp.requests.AddTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.requests.ChangeTotpDeviceNameRequest
import com.supertokens.sdk.recipes.totp.requests.RemoveTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.requests.VerifyTotpCodeRequest
import com.supertokens.sdk.recipes.totp.requests.VerifyTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.responses.AddTotpDeviceResponse
import com.supertokens.sdk.recipes.totp.responses.RemoveTotpDeviceResponse
import com.supertokens.sdk.recipes.totp.responses.TotpDevicesResponse
import com.supertokens.sdk.recipes.totp.responses.VerifyTotpDeviceResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class TotpRecipeConfig: RecipeConfig {

    var defaultSkew: Int = 1
    var defaultPeriod: Int = 30
    var issuer: String? = null

}

class TotpRecipe(
    private val superTokens: SuperTokens,
    private val config: TotpRecipeConfig
): Recipe<TotpRecipeConfig> {

    val defaultSkew = config.defaultSkew
    val defaultPeriod = config.defaultPeriod
    val issuer by lazy {
        config.issuer ?: superTokens.appConfig.name
    }

    /**
     * Add a TOTP device for a user and enable TOTP if not already enabled.
     *
     * @return the TOTP device secret
     */
    suspend fun addDevice(userId: String, deviceName: String, skew: Int? = null, period: Int? = null): String {
        val response = superTokens.client.post(PATH_TOTP_DEVICE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                AddTotpDeviceRequest(
                    userId = userId,
                    deviceName = deviceName,
                    skew = skew ?: defaultSkew,
                    period = period ?: defaultPeriod,
                )
            )
        }

        return response.parse<AddTotpDeviceResponse, String> {
            checkNotNull(it.secret)
        }
    }

    /**
     * Update the name of a TOTP device for a user.
     */
    suspend fun changeDeviceName(userId: String, oldDeviceName: String, newDeviceName: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_TOTP_DEVICE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ChangeTotpDeviceNameRequest(
                    userId = userId,
                    existingDeviceName = oldDeviceName,
                    newDeviceName = newDeviceName,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    /**
     * Retrieve a list of TOTP devices for a user.
     */
    suspend fun getDevices(userId: String): List<TotpDevice> {
        val response = superTokens.client.get("$PATH_TOTP_DEVICES?userId=$userId") {

            header(Constants.HEADER_RECIPE_ID, SessionRecipe.ID)
        }

        return response.parse<TotpDevicesResponse, List<TotpDevice>> {
            requireNotNull(it.devices).map { device ->
                TotpDevice(
                    name = device.name,
                    period = device.period,
                    skew = device.skew,
                    verified = device.verified,
                )
            }
        }
    }

    /**
     * Remove a TOTP device for a user. If all devices are removed, TOTP is disabled for the user.
     *
     * @return true, if the device existed
     */
    suspend fun removeDevice(userId: String, deviceName: String): Boolean {
        val response = superTokens.client.post(PATH_TOTP_DEVICE_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RemoveTotpDeviceRequest(
                    userId = userId,
                    deviceName = deviceName,
                )
            )
        }

        return response.parse<RemoveTotpDeviceResponse, Boolean> {
            it.didDeviceExist == true
        }
    }

    /**
     * Check if a TOTP code is valid against any of the TOTP devices for a user.
     */
    suspend fun verifyCode(userId: String, totp: String): Boolean {
        val response = superTokens.client.post(PATH_TOTP_CODE_VERIFY) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                VerifyTotpCodeRequest(
                    userId = userId,
                    totp = totp,
                )
            )
        }

        return response.parse<StatusResponse, Boolean> {
            it.status == SuperTokensStatus.OK.value
        }
    }

    /**
     * Mark a TOTP device as verified if the given TOTP code is valid for that device.
     *
     * @return true, if the device was already verified
     */
    suspend fun verifyDevice(userId: String, deviceName: String, totp: String): Boolean {
        val response = superTokens.client.post(PATH_TOTP_DEVICE_VERIFY) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                VerifyTotpDeviceRequest(
                    userId = userId,
                    deviceName = deviceName,
                    totp = totp,
                )
            )
        }

        return response.parse<VerifyTotpDeviceResponse, Boolean> {
            it.wasAlreadyVerified == true
        }
    }

    companion object {
        const val ID = "totp"

        const val PATH_TOTP_DEVICE = "/recipe/totp/device"
        const val PATH_TOTP_DEVICES = "/recipe/totp/device/list"
        const val PATH_TOTP_DEVICE_REMOVE = "/recipe/totp/device/remove"
        const val PATH_TOTP_CODE_VERIFY = "/recipe/totp/verify"
        const val PATH_TOTP_DEVICE_VERIFY = "/recipe/totp/device/verify"
    }

}

val Totp = object: RecipeBuilder<TotpRecipeConfig, TotpRecipe>() {

    override fun install(configure: TotpRecipeConfig.() -> Unit): (SuperTokens) -> TotpRecipe {
        val config = TotpRecipeConfig().apply(configure)

        return {
            TotpRecipe(it, config)
        }
    }

}

/**
 * Add a TOTP device for a user and enable TOTP if not already enabled.
 *
 * @return the TOTP device secret
 */
suspend fun SuperTokens.addTotpDevice(
    userId: String,
    deviceName: String,
    skew: Int? = null,
    period: Int? = null,
) = getRecipe<TotpRecipe>().addDevice(userId, deviceName, skew, period)

/**
 * Retrieve a list of TOTP devices for a user.
 */
suspend fun SuperTokens.getTotpDevices(
    userId: String,
) = getRecipe<TotpRecipe>().getDevices(userId)

/**
 * Update the name of a TOTP device for a user.
 */
suspend fun SuperTokens.changeTotpDeviceName(
    userId: String,
    oldDeviceName: String,
    newDeviceName: String
) = getRecipe<TotpRecipe>().changeDeviceName(userId, oldDeviceName, newDeviceName)

/**
 * Remove a TOTP device for a user. If all devices are removed, TOTP is disabled for the user.
 *
 * @return true, if the device existed
 */
suspend fun SuperTokens.removeTotpDevice(
    userId: String,
    deviceName: String,
) = getRecipe<TotpRecipe>().removeDevice(userId, deviceName)

/**
 * Check if a TOTP code is valid against any of the TOTP devices for a user.
 */
suspend fun SuperTokens.verifyTotpCode(
    userId: String,
    totp: String,
) = getRecipe<TotpRecipe>().verifyCode(userId, totp)

/**
 * Mark a TOTP device as verified if the given TOTP code is valid for that device.
 *
 * @return true, if the device was already verified
 */
suspend fun SuperTokens.verifyTotpDevice(
    userId: String,
    deviceName: String,
    totp: String,
) = getRecipe<TotpRecipe>().verifyDevice(userId, deviceName, totp)
