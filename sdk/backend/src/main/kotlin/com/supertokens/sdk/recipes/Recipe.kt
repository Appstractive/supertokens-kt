package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.SuperTokensRecipeDslMarker
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.common.models.User

@SuperTokensRecipeDslMarker interface RecipeConfig

interface Recipe<C : RecipeConfig> {

  suspend fun getExtraJwtData(
      user: User,
      tenantId: String?,
      recipeId: String,
      multiAuthFactor: AuthFactor?,
      accessToken: String?,
  ): Map<String, Any?> = emptyMap()
}

typealias CustomJwtData = suspend (superTokens: SuperTokens, user: User) -> Map<String, Any?>

typealias BuildRecipe = (SuperTokens) -> Recipe<*>

abstract class RecipeBuilder<C : RecipeConfig, R : Recipe<C>> {

  abstract fun install(configure: C.() -> Unit): BuildRecipe
}
