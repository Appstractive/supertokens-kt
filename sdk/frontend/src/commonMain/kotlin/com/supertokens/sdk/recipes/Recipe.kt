package com.supertokens.sdk.recipes

import com.supertokens.sdk.SuperTokensClient
import io.ktor.client.HttpClientConfig

interface RecipeConfig
interface Recipe<C: RecipeConfig> {

    suspend fun postInit() {}

    fun HttpClientConfig<*>.configureClient() {}

}

typealias BuildRecipe = (SuperTokensClient) -> Recipe<*>

abstract class RecipeBuilder<C: RecipeConfig, R: Recipe<C>> {

    abstract fun install(configure: C.() -> Unit): BuildRecipe

}