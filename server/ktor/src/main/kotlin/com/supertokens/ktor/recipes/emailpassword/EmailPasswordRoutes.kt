package com.supertokens.ktor.recipes.emailpassword

import com.supertokens.ktor.plugins.SuperTokensAuth
import com.supertokens.ktor.superTokens
import com.supertokens.sdk.common.Routes
import com.supertokens.sdk.recipes.session.SessionRecipe
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.emailPasswordRoutes(handler: EmailPasswordHandler) {

  post(Routes.EmailPassword.SIGN_IN) { with(handler) { signIn() } }

  post(Routes.EmailPassword.SIGN_UP) { with(handler) { signUp() } }

  post(Routes.EmailPassword.PASSWORD_RESET_TOKEN) { with(handler) { passwordResetWithToken() } }

  post(Routes.EmailPassword.PASSWORD_RESET) { with(handler) { resetPassword() } }

  if (superTokens.hasRecipe<SessionRecipe>()) {
    authenticate(SuperTokensAuth) {
      post(Routes.EmailPassword.PASSWORD_CHANGE) { with(handler) { changePassword() } }
    }
  }
}
