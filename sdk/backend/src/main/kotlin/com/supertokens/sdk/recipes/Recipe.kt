package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokens

interface RecipeConfig
interface Recipe<C: RecipeConfig>

typealias BuildRecipe = (SuperTokens) -> Recipe<*>

abstract class RecipeBuilder<C: RecipeConfig, R: Recipe<C>> {

    abstract fun install(configure: C.() -> Unit): BuildRecipe

}