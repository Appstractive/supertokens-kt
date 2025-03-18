package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.sessions: SessionRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.sessions: SessionRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.sessions: SessionRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.sessions: SessionRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isSessionsEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
val PipelineContext<Unit, ApplicationCall>.isSessionsEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
val Route.isSessionsEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
val RoutingContext.isSessionsEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
