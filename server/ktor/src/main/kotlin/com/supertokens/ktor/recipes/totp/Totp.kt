package com.supertokens.ktor.recipes.totp

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.totp.TotpRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.totp: TotpRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.totp: TotpRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.totp: TotpRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.totp: TotpRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isTotpEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<TotpRecipe>()
val PipelineContext<Unit, ApplicationCall>.isTotpEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<TotpRecipe>()
val Route.isTotpEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<TotpRecipe>()
val RoutingContext.isTotpEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<TotpRecipe>()
