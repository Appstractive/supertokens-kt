package com.supertokens.sdk.recipes.multifactor

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.multifactor.usecases.CheckMultiFactorStatusUseCase

class MultiFactorAuthConfig: RecipeConfig

class MultiFactorAuthRecipe(
    private val superTokens: SuperTokensClient,
    private val config: MultiFactorAuthConfig,
) : Recipe<MultiFactorAuthConfig> {

    private val checkMultiFactorStatusUseCase by lazy {
        CheckMultiFactorStatusUseCase(
            client = superTokens.apiClient,
        )
    }

    suspend fun checkMfaStatus() = checkMultiFactorStatusUseCase.checkStatus()

}

object MultiFactorAuth : RecipeBuilder<MultiFactorAuthConfig, MultiFactorAuthRecipe>() {
    override fun install(configure: MultiFactorAuthConfig.() -> Unit): (SuperTokensClient) -> MultiFactorAuthRecipe {
        val config = MultiFactorAuthConfig().apply(configure)

        return {
            MultiFactorAuthRecipe(it, config)
        }
    }
}
