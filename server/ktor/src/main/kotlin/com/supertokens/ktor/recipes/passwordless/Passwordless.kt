package com.supertokens.ktor.recipes.passwordless

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.passwordless: PasswordlessRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.passwordless: PasswordlessRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.passwordless: PasswordlessRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isPasswordlessEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val PipelineContext<Unit, ApplicationCall>.isPasswordlessEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
val Route.isPasswordlessEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<PasswordlessRecipe>()
