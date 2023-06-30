package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.ktor.SuperTokensAttributeKey
import com.supertokens.sdk.recipes.emailpassword.EmailPasswordRecipe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.pipeline.PipelineContext

val PipelineContext<Unit, ApplicationCall>.emailPassword: EmailPasswordRecipe get() =
    application.attributes[SuperTokensAttributeKey].getRecipe()
val Route.emailPassword: EmailPasswordRecipe get() =
    application.attributes[SuperTokensAttributeKey].getRecipe()

val PipelineContext<Unit, ApplicationCall>.isEmailPasswordEnabled: Boolean get() =
    application.attributes[SuperTokensAttributeKey].hasRecipe<EmailPasswordRecipe>()
val Route.isEmailPasswordEnabled: Boolean get() =
    application.attributes[SuperTokensAttributeKey].hasRecipe<EmailPasswordRecipe>()