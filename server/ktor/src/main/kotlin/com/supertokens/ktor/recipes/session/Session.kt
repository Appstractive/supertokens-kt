package com.supertokens.ktor.recipes.session

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val ApplicationCall.sessions: SessionRecipe
    get() =
        application.attributes[SuperTokensAttributeKey].getRecipe()
val PipelineContext<Unit, ApplicationCall>.sessions: SessionRecipe
    get() =
    application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.sessions: SessionRecipe
    get() =
    application.attributes[SuperTokensAttributeKey].getRecipe()

val ApplicationCall.sessionsEnabled: Boolean get() =
    application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
val PipelineContext<Unit, ApplicationCall>.sessionsEnabled: Boolean get() =
    application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()
val Route.sessionsEnabled: Boolean get() =
    application.attributes[SuperTokensAttributeKey].hasRecipe<SessionRecipe>()