package com.supertokens.sdk.recipes.totp

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.BuildRecipe
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.totp.usecases.CreateTotpDeviceResult
import com.supertokens.sdk.recipes.totp.usecases.CreateTotpDeviceUseCase
import com.supertokens.sdk.recipes.totp.usecases.GetTotpDevicesUseCase
import com.supertokens.sdk.recipes.totp.usecases.RemoveTotpDeviceUseCase
import com.supertokens.sdk.recipes.totp.usecases.VerifyTotpDeviceUseCase
import com.supertokens.sdk.recipes.totp.usecases.VerifyTotpUseCase

class TotpConfig : RecipeConfig

class TotpRecipe(
    private val superTokens: SuperTokensClient,
    private val config: TotpConfig,
) : Recipe<TotpConfig> {

    private val getTotpDevicesUseCase by lazy {
        GetTotpDevicesUseCase(
            client = superTokens.apiClient,
        )
    }

    private val createDeviceUseCase by lazy {
        CreateTotpDeviceUseCase(
            client = superTokens.apiClient,
        )
    }

    private val removeDeviceUseCase by lazy {
        RemoveTotpDeviceUseCase(
            client = superTokens.apiClient,
        )
    }

    private val verifyDeviceUseCase by lazy {
        VerifyTotpDeviceUseCase(
            client = superTokens.apiClient,
        )
    }

    private val verifyUseCase by lazy {
        VerifyTotpUseCase(
            client = superTokens.apiClient,
        )
    }

    suspend fun getDevices() = getTotpDevicesUseCase.getTotpDevices()
    suspend fun createDevice(name: String): CreateTotpDeviceResult = createDeviceUseCase.createDevice(name = name)
    suspend fun removeDevice(name: String) = removeDeviceUseCase.removeDevice(name = name)
    suspend fun verifyDevice(name: String, totp: String) =
        verifyDeviceUseCase.verifyDevice(deviceName = name, totp = totp)

    suspend fun verify(totp: String) = verifyUseCase.verifyCode(totp = totp)

}

object Totp : RecipeBuilder<TotpConfig, TotpRecipe>() {

    override fun install(configure: TotpConfig.() -> Unit): BuildRecipe {
        val config = TotpConfig().apply(configure)

        return {
            TotpRecipe(it, config)
        }
    }

}