package com.supertokens.sdk.recipes.totp

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_TOTP
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.responses.GetTotpDevicesResponseDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.get
import com.supertokens.sdk.post
import com.supertokens.sdk.put
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.totp.models.TotpDevice
import com.supertokens.sdk.recipes.totp.requests.AddTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.requests.ChangeTotpDeviceNameRequest
import com.supertokens.sdk.recipes.totp.requests.RemoveTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.requests.VerifyTotpCodeRequest
import com.supertokens.sdk.recipes.totp.requests.VerifyTotpDeviceRequest
import com.supertokens.sdk.recipes.totp.responses.AddTotpDeviceResponseDTO
import com.supertokens.sdk.recipes.totp.responses.ImportTotpDeviceResponseDTO
import com.supertokens.sdk.recipes.totp.responses.RemoveTotpDeviceResponseDTO
import com.supertokens.sdk.recipes.totp.responses.VerifyTotpDeviceResponseDTO
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.header
import io.ktor.client.request.setBody

class TotpRecipeConfig : RecipeConfig {

  var defaultSkew: Int = 1
  var defaultPeriod: Int = 30
  var issuer: String? = null
}

class TotpRecipe(private val superTokens: SuperTokens, private val config: TotpRecipeConfig) :
    Recipe<TotpRecipeConfig> {

  private val defaultSkew = config.defaultSkew
  private val defaultPeriod = config.defaultPeriod
  val issuer by lazy { config.issuer ?: superTokens.appConfig.name }

  /**
   * Add a TOTP device for a user and enable TOTP if not already enabled.
   *
   * @return the TOTP device secret
   */
  @Throws(SuperTokensStatusException::class)
  suspend fun addDevice(
      userId: String,
      deviceName: String,
      skew: Int? = null,
      period: Int? = null
  ): String {
    val response =
        superTokens.post(PATH_TOTP_DEVICE, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              AddTotpDeviceRequest(
                  userId = userId,
                  deviceName = deviceName,
                  skew = skew ?: defaultSkew,
                  period = period ?: defaultPeriod,
              ))
        }

    return response.parse<AddTotpDeviceResponseDTO, String> { checkNotNull(it.secret) }
  }

  /** Update the name of a TOTP device for a user. */
  @Throws(SuperTokensStatusException::class)
  suspend fun changeDeviceName(
      userId: String,
      oldDeviceName: String,
      newDeviceName: String
  ): SuperTokensStatus {
    val response =
        superTokens.put(PATH_TOTP_DEVICE, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              ChangeTotpDeviceNameRequest(
                  userId = userId,
                  existingDeviceName = oldDeviceName,
                  newDeviceName = newDeviceName,
              ))
        }

    return response.parse<StatusResponseDTO, SuperTokensStatus> { it.status.toStatus() }
  }

  /**
   * Add a TOTP device for a user and enable TOTP if not already enabled.
   *
   * @return the TOTP device name
   */
  @Throws(SuperTokensStatusException::class)
  suspend fun importDevice(
      userId: String,
      deviceName: String,
      secretKey: String,
      skew: Int? = null,
      period: Int? = null
  ): String {
    val response =
        superTokens.post(PATH_TOTP_DEVICE_IMPORT, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              AddTotpDeviceRequest(
                  userId = userId,
                  deviceName = deviceName,
                  skew = skew ?: defaultSkew,
                  period = period ?: defaultPeriod,
                  secretKey = secretKey,
              ))
        }

    return response.parse<ImportTotpDeviceResponseDTO, String> { checkNotNull(it.deviceName) }
  }

  /** Retrieve a list of TOTP devices for a user. */
  @Throws(SuperTokensStatusException::class)
  suspend fun getDevices(userId: String): List<TotpDevice> {
    val response =
        superTokens.get(
            PATH_TOTP_DEVICES, tenantId = null, queryParams = mapOf("userId" to userId)) {
              header(HEADER_RECIPE_ID, RECIPE_TOTP)
            }

    return response.parse<GetTotpDevicesResponseDTO, List<TotpDevice>> {
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
  @Throws(SuperTokensStatusException::class)
  suspend fun removeDevice(userId: String, deviceName: String): Boolean {
    val response =
        superTokens.post(PATH_TOTP_DEVICE_REMOVE, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              RemoveTotpDeviceRequest(
                  userId = userId,
                  deviceName = deviceName,
              ))
        }

    return response.parse<RemoveTotpDeviceResponseDTO, Boolean> { it.didDeviceExist == true }
  }

  /** Check if a TOTP code is valid against any of the TOTP devices for a user. */
  @Throws(SuperTokensStatusException::class)
  suspend fun verifyCode(
      userId: String,
      totp: String,
      allowUnverifiedDevices: Boolean = false,
      tenantId: String?
  ): SuperTokensStatus {
    val response =
        superTokens.post(PATH_TOTP_CODE_VERIFY, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              VerifyTotpCodeRequest(
                  userId = userId,
                  totp = totp,
                  allowUnverifiedDevices = allowUnverifiedDevices,
              ))
        }

    return response.parse()
  }

  /**
   * Mark a TOTP device as verified if the given TOTP code is valid for that device.
   *
   * @return true, if the device was already verified
   */
  @Throws(SuperTokensStatusException::class)
  suspend fun verifyDevice(
      userId: String,
      deviceName: String,
      totp: String,
      tenantId: String?,
  ): Boolean {
    val response =
        superTokens.post(PATH_TOTP_DEVICE_VERIFY, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_TOTP)

          setBody(
              VerifyTotpDeviceRequest(
                  userId = userId,
                  deviceName = deviceName,
                  totp = totp,
              ))
        }

    return response.parse<VerifyTotpDeviceResponseDTO, Boolean> { it.wasAlreadyVerified == true }
  }

  companion object {
    const val PATH_TOTP_DEVICE = "/recipe/totp/device"
    const val PATH_TOTP_DEVICES = "/recipe/totp/device/list"
    const val PATH_TOTP_DEVICE_IMPORT = "/recipe/totp/device/import"
    const val PATH_TOTP_DEVICE_REMOVE = "/recipe/totp/device/remove"
    const val PATH_TOTP_CODE_VERIFY = "/recipe/totp/verify"
    const val PATH_TOTP_DEVICE_VERIFY = "/recipe/totp/device/verify"
  }
}

val Totp =
    object : RecipeBuilder<TotpRecipeConfig, TotpRecipe>() {

      override fun install(configure: TotpRecipeConfig.() -> Unit): (SuperTokens) -> TotpRecipe {
        val config = TotpRecipeConfig().apply(configure)

        return { TotpRecipe(it, config) }
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
) =
    getRecipe<TotpRecipe>()
        .addDevice(
            userId = userId,
            deviceName = deviceName,
            skew = skew,
            period = period,
        )

/**
 * Add a TOTP device for a user and enable TOTP if not already enabled.
 *
 * @return the TOTP device secret
 */
suspend fun SuperTokens.importTotpDevice(
    userId: String,
    deviceName: String,
    secretKey: String,
    skew: Int? = null,
    period: Int? = null,
) =
    getRecipe<TotpRecipe>()
        .importDevice(
            userId = userId,
            deviceName = deviceName,
            secretKey = secretKey,
            skew = skew,
            period = period,
        )

/** Retrieve a list of TOTP devices for a user. */
suspend fun SuperTokens.getTotpDevices(
    userId: String,
) = getRecipe<TotpRecipe>().getDevices(userId)

/** Update the name of a TOTP device for a user. */
suspend fun SuperTokens.changeTotpDeviceName(
    userId: String,
    oldDeviceName: String,
    newDeviceName: String
) =
    getRecipe<TotpRecipe>()
        .changeDeviceName(
            userId = userId, oldDeviceName = oldDeviceName, newDeviceName = newDeviceName)

/**
 * Remove a TOTP device for a user. If all devices are removed, TOTP is disabled for the user.
 *
 * @return true, if the device existed
 */
suspend fun SuperTokens.removeTotpDevice(
    userId: String,
    deviceName: String,
) = getRecipe<TotpRecipe>().removeDevice(userId = userId, deviceName = deviceName)

/** Check if a TOTP code is valid against any of the TOTP devices for a user. */
suspend fun SuperTokens.verifyTotpCode(
    userId: String,
    totp: String,
    allowUnverifiedDevices: Boolean = false,
    tenantId: String? = null,
) =
    getRecipe<TotpRecipe>()
        .verifyCode(
            userId = userId,
            totp = totp,
            allowUnverifiedDevices = allowUnverifiedDevices,
            tenantId = tenantId)

/**
 * Mark a TOTP device as verified if the given TOTP code is valid for that device.
 *
 * @return true, if the device was already verified
 */
suspend fun SuperTokens.verifyTotpDevice(
    userId: String,
    deviceName: String,
    totp: String,
    tenantId: String? = null,
) =
    getRecipe<TotpRecipe>()
        .verifyDevice(userId = userId, deviceName = deviceName, totp = totp, tenantId = tenantId)
