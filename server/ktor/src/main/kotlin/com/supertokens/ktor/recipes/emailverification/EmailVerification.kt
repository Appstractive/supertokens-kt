package com.supertokens.ktor.recipes.emailverification

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.emailverification.EmailVerificationRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.emailVerification: EmailVerificationRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.emailVerification: EmailVerificationRecipe
  get() = context.attributes[SuperTokensAttributeKey].getRecipe()
val Route.emailVerification: EmailVerificationRecipe
  get() = application.attributes[SuperTokensAttributeKey].getRecipe()
val RoutingContext.emailVerification: EmailVerificationRecipe
  get() = call.application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.isEmailVerificationEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<EmailVerificationRecipe>()
val PipelineContext<Unit, ApplicationCall>.isEmailVerificationEnabled: Boolean
  get() = context.attributes[SuperTokensAttributeKey].hasRecipe<EmailVerificationRecipe>()
val Route.isEmailVerificationEnabled: Boolean
  get() = application.attributes[SuperTokensAttributeKey].hasRecipe<EmailVerificationRecipe>()
val RoutingContext.isEmailVerificationEnabled: Boolean
  get() = call.application.attributes[SuperTokensAttributeKey].hasRecipe<EmailVerificationRecipe>()
