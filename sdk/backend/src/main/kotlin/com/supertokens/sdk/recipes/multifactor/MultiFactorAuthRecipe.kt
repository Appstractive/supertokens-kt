package com.supertokens.sdk.recipes.multifactor

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.session.verifySession

typealias GetRequiredMultiFactors = suspend (superTokens: SuperTokens, user: User, tenantId: String?) -> List<AuthFactor>

class MultiFactorRecipeConfig : RecipeConfig {

    var firstFactors: List<String> = emptyList()

    // TODO add default implementation with factors from tenant
    var getRequiredMultiFactors: GetRequiredMultiFactors = { _, _, _ -> emptyList() }

}

class MultiFactorAuthRecipe(
    private val superTokens: SuperTokens,
    private val config: MultiFactorRecipeConfig
) : Recipe<MultiFactorRecipeConfig> {

    val firstFactors = config.firstFactors
    val getRequiredMultiFactors = config.getRequiredMultiFactors

    override suspend fun getExtraJwtData(user: User, tenantId: String?, recipeId: String, accessToken: String?): Map<String, Any?> {
        val userDataInJWT = accessToken?.let { token ->
            runCatching {
                superTokens.verifySession(
                    accessToken = token,
                    checkDatabase = true,
                ).session
            }.getOrNull()?.userDataInJWT
        } ?: emptyMap()

        val mfaData = (userDataInJWT[Claims.MFA] as? Map<String, Any?>) ?: emptyMap()
        val factors = mfaData.getFactors().toMutableMap().apply {
            if(!contains(recipeId)) {
                put(recipeId, System.currentTimeMillis())
            }
        }

        val hasFirstFactor = firstFactors.any { first -> factors.keys.any { it == first } }
        val requiredSecondFactors = getRequiredMultiFactors(
            superTokens,
            user,
            tenantId,
        )
        val hasSecondFactor = hasRequiredSecondsFactors(
            factors = factors,
            requiredFactors = requiredSecondFactors,
        )

        return mapOf(
            Claims.MFA to mapOf<String, Any?>(
                Claims.MFA_FACTORS to factors,
                Claims.MFA_VERIFIED to (hasFirstFactor && hasSecondFactor),
            )
        )
    }

    private fun Map<String, Any?>.getFactors(): Map<String, Number> {
        return get(Claims.MFA_FACTORS) as? Map<String, Number> ?: emptyMap()
    }

    private fun hasRequiredSecondsFactors(factors: Map<String, Number>, requiredFactors: List<AuthFactor>): Boolean {
        return requiredFactors.all { it.isValidFor(factors) }
    }

}

val MultiFactorAuth = object : RecipeBuilder<MultiFactorRecipeConfig, MultiFactorAuthRecipe>() {

    override fun install(configure: MultiFactorRecipeConfig.() -> Unit): (SuperTokens) -> MultiFactorAuthRecipe {
        val config = MultiFactorRecipeConfig().apply(configure)

        return {
            MultiFactorAuthRecipe(it, config)
        }
    }
}
