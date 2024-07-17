package com.supertokens.ktor.recipes.multifactor

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.multifactor.MultiFactorAuthRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.multiFactorAuth: MultiFactorAuthRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.multiFactorAuth: MultiFactorAuthRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.multiFactorAuth: MultiFactorAuthRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isMultiFactorAuthEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<MultiFactorAuthRecipe>()
val PipelineContext<Unit, ApplicationCall>.isMultiFactorAuthEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<MultiFactorAuthRecipe>()
val Route.isMultiFactorAuthEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<MultiFactorAuthRecipe>()
