package com.supertokens.sdk.recipes.multifactor

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.RECIPE_TOTP
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.common.models.AuthFactor.OTP_PHONE.isValid
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.session.verifySession

typealias GetRequiredMultiFactors =
    suspend (superTokens: SuperTokens, user: User, tenantId: String?) -> List<AuthFactor>

class MultiFactorRecipeConfig : RecipeConfig {

  var firstFactors: List<String> = emptyList()

  // TODO add default implementation with factors from tenant
  var getRequiredMultiFactors: GetRequiredMultiFactors = { _, _, _ -> emptyList() }
}

class MultiFactorAuthRecipe(
    private val superTokens: SuperTokens,
    private val config: MultiFactorRecipeConfig,
) : Recipe<MultiFactorRecipeConfig> {

  val firstFactors = config.firstFactors
  val getRequiredMultiFactors = config.getRequiredMultiFactors

  override suspend fun getExtraJwtData(
      user: User,
      tenantId: String?,
      recipeId: String,
      multiAuthFactor: AuthFactor?,
      accessToken: String?
  ): Map<String, Any?> {
    val userDataInJWT =
        accessToken?.let { token ->
          runCatching {
                val sessionData =
                    superTokens.verifySession(
                        accessToken = token,
                        checkDatabase = true,
                    )
                sessionData.session
              }
              .getOrNull()
              ?.userDataInJWT
        } ?: emptyMap()

    val factors =
        getFactorsFromJwtData(userDataInJWT).toMutableMap().apply {
          when (recipeId) {
            RECIPE_EMAIL_PASSWORD -> {
              put(recipeId, System.currentTimeMillis())
            }
            RECIPE_PASSWORDLESS -> {
              when (multiAuthFactor) {
                AuthFactor.OTP_EMAIL -> put(AuthFactor.OTP_EMAIL.key, System.currentTimeMillis())
                AuthFactor.OTP_EMAIL -> put(AuthFactor.LINK_EMAIL.key, System.currentTimeMillis())
                AuthFactor.OTP_PHONE -> put(AuthFactor.OTP_PHONE.key, System.currentTimeMillis())
                AuthFactor.OTP_PHONE -> put(AuthFactor.LINK_PHONE.key, System.currentTimeMillis())
                else -> {}
              }
            }
            RECIPE_TOTP -> {
              put(AuthFactor.TOTP.key, System.currentTimeMillis())
            }
          }
        }

    val hasFirstFactor = firstFactors.any { first -> factors.keys.any { it == first } }
    val requiredSecondFactors =
        getRequiredMultiFactors(
            superTokens,
            user,
            tenantId,
        )
    val hasSecondFactor = requiredSecondFactors.isValid(factors)

    return mapOf(
        Claims.MFA to
            mapOf<String, Any?>(
                Claims.MFA_FACTORS to factors,
                Claims.MFA_VERIFIED to (hasFirstFactor && hasSecondFactor),
            ))
  }

  fun getFactorsFromJwtData(userDataInJWT: Map<String, Any?>): Map<String, Number> {
    val mfaData = (userDataInJWT[Claims.MFA] as? Map<String, Any?>) ?: emptyMap()
    return mfaData.getFactors()
  }
}

internal fun Map<String, Any?>.getFactors(): Map<String, Number> {
  return get(Claims.MFA_FACTORS) as? Map<String, Number> ?: emptyMap()
}

val MultiFactorAuth =
    object : RecipeBuilder<MultiFactorRecipeConfig, MultiFactorAuthRecipe>() {

      override fun install(
          configure: MultiFactorRecipeConfig.() -> Unit
      ): (SuperTokens) -> MultiFactorAuthRecipe {
        val config = MultiFactorRecipeConfig().apply(configure)

        check(config.firstFactors.isNotEmpty()) { "firstFactors may not be empty" }

        return { MultiFactorAuthRecipe(it, config) }
      }
    }
