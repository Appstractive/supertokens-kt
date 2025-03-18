package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.passwordless: PasswordlessRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.passwordless: PasswordlessRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.passwordless: PasswordlessRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.passwordless: PasswordlessRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isPasswordlessEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val PipelineContext<Unit, ApplicationCall>.isPasswordlessEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val Route.isPasswordlessEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val RoutingContext.isPasswordlessEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
