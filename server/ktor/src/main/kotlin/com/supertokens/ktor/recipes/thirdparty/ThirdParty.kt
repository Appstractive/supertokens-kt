package com.supertokens.ktor.recipes.thirdparty

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.thirdparty.ThirdPartyRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.thirdParty: ThirdPartyRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.thirdParty: ThirdPartyRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.thirdParty: ThirdPartyRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.thirdParty: ThirdPartyRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isThirdPartyEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
val PipelineContext<Unit, ApplicationCall>.isThirdPartyEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
val Route.isThirdPartyEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
val RoutingContext.isThirdPartyEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<ThirdPartyRecipe>()
