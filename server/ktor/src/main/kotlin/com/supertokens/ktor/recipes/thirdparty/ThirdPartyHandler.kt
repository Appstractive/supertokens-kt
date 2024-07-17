package com.supertokens.ktor.recipes.thirdparty

import com.supertokens.ktor.recipes.session.isSessionsEnabled
import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.userHandler
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.NotFoundException
import com.supertokens.ktor.utils.frontend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.ktor.utils.tenantId
import com.supertokens.sdk.common.RECIPE_THIRD_PARTY
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.common.requests.ThirdPartySignInUpRequestDTO
import com.supertokens.sdk.common.responses.AuthorizationUrlResponseDTO
import com.supertokens.sdk.common.responses.SignInUpResponseDTO
import com.supertokens.sdk.common.util.generateCodeVerifier
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

open class ThirdPartyHandler(
    protected val scope: CoroutineScope,
) {

  open suspend fun PipelineContext<Unit, ApplicationCall>.handleMissingEmail(
      provider: Provider<*>,
      userInfo: ThirdPartyUserInfo
  ): String {
    return "${userInfo.id}@${provider.id}.temp"
  }

  /**
   * A call to POST /signinup
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/signInUp">Frontend
   *   Driver Interface</a>
   */
  open suspend fun PipelineContext<Unit, ApplicationCall>.signInUp() {
    val body = call.receive<ThirdPartySignInUpRequestDTO>()
    val provider = thirdParty.getProviderById(body.thirdPartyId) ?: throw NotFoundException()

    val tokens =
        body.redirectURIInfo?.let {
          provider.getTokens(
              parameters = it.redirectURIQueryParams,
              pkceCodeVerifier = it.pkceCodeVerifier,
              redirectUrl = it.redirectURIOnProviderDashboard)
        } ?: body.oAuthTokens ?: throw BadRequestException()

    val userInfo = provider.getUserInfo(tokens)

    val response =
        thirdParty.signInUp(
            thirdPartyId = body.thirdPartyId,
            thirdPartyUserId = userInfo.id,
            email = userInfo.email?.id ?: handleMissingEmail(provider, userInfo),
            tenantId = call.tenantId,
            isVerified = userInfo.email?.isVerified == true,
        )

    with(userHandler) {
      if (response.createdNewUser) {
        onUserSignedUp(response.user)
      } else {
        onUserSignedIn(response.user)
      }
    }

    if (isSessionsEnabled) {
      val session =
          sessions.createSession(
              userId = response.user.id,
              tenantId = call.tenantId,
              userDataInJWT =
                  sessions.getJwtData(
                      user = response.user,
                      tenantId = call.tenantId,
                      recipeId = RECIPE_THIRD_PARTY,
                      multiAuthFactor = null,
                      accessToken = null,
                  ),
          )

      setSessionInResponse(
          accessToken = session.accessToken,
          refreshToken = session.refreshToken,
          antiCsrfToken = session.antiCsrfToken,
      )
    }

    call.respond(
        SignInUpResponseDTO(
            user = response.user,
            createdNewUser = response.createdNewUser,
        ),
    )
  }

  /**
   * A call to GET /authorisationurl
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/authorisationUrl">Frontend
   *   Driver Interface</a>
   */
  open suspend fun PipelineContext<Unit, ApplicationCall>.getAuthorizationUrl() {
    val thirdPartyId = call.parameters["thirdPartyId"] ?: throw NotFoundException()
    val redirectURIOnProviderDashboard =
        call.parameters["redirectURIOnProviderDashboard"] ?: throw NotFoundException()

    val provider = thirdParty.getProviderById(thirdPartyId) ?: throw NotFoundException()

    call.respond(
        AuthorizationUrlResponseDTO(
            urlWithQueryParams =
                provider.getAuthorizationEndpoint(redirectURIOnProviderDashboard).fullUrl,
            pkceCodeVerifier = generateCodeVerifier(),
        ),
    )
  }

  /**
   * A call to POST /callback/apple
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/thirdPartyCallbackApple">Frontend
   *   Driver Interface</a>
   */
  open suspend fun PipelineContext<Unit, ApplicationCall>.appleAuthCallback() {
    val provider = thirdParty.getProviderById(ThirdPartyProvider.APPLE) ?: throw NotFoundException()
    val formParameters = call.receiveParameters()
    val code =
        formParameters["code"]
            ?: throw BadRequestException(message = "Form Param 'code' is required")
    val state = formParameters["state"]

    val tokens =
        provider.getTokens(
            mapOf("code" to code),
            null,
            null,
        )
    val userInfo = provider.getUserInfo(tokens)

    val response =
        thirdParty.signInUp(
            thirdPartyId = provider.id,
            thirdPartyUserId = userInfo.id,
            email = userInfo.email?.id ?: handleMissingEmail(provider, userInfo),
            tenantId = call.tenantId,
            isVerified = userInfo.email?.isVerified == true,
        )

    if (isSessionsEnabled) {
      val session =
          sessions.createSession(
              userId = response.user.id,
              tenantId = call.tenantId,
              userDataInJWT =
                  sessions.getJwtData(
                      user = response.user,
                      tenantId = call.tenantId,
                      recipeId = RECIPE_THIRD_PARTY,
                      multiAuthFactor = null,
                      accessToken = null,
                  ),
          )

      setSessionInResponse(
          accessToken = session.accessToken,
          refreshToken = session.refreshToken,
          antiCsrfToken = session.antiCsrfToken,
      )
    }

    call.respondRedirect {
      protocol = URLProtocol.HTTPS
      host = call.frontend.host
      path(call.frontend.path)
    }
  }
}
