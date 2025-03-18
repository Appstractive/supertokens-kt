package com.appstractive

import com.supertokens.ktor.recipes.emailpassword.EmailPasswordHandler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.RoutingContext
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

class CustomEmailPasswordHandler(
    scope: CoroutineScope,
) : EmailPasswordHandler(scope) {

  private val defaultEmailPasswordHandler = EmailPasswordHandler(scope)

  override suspend fun RoutingContext.signIn() {
    // TODO we can't use super.signin() at the moment (see
    // https://youtrack.jetbrains.com/issue/KT-11488)
    with(defaultEmailPasswordHandler) {
      // call default implementation
      signIn()
    }
  }
}
