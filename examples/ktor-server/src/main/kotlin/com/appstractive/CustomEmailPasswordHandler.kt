package com.appstractive

import com.supertokens.ktor.recipes.emailpassword.EmailPasswordHandler
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

class CustomEmailPasswordHandler: EmailPasswordHandler() {

    private val defaultEmailPasswordHandler = EmailPasswordHandler()

    override suspend fun PipelineContext<Unit, ApplicationCall>.signIn() {
        // TODO we can't use super.signin() at the moment (see https://youtrack.jetbrains.com/issue/KT-11488)
        with(defaultEmailPasswordHandler) {
            // call default implementation
            signIn()
        }
    }

}