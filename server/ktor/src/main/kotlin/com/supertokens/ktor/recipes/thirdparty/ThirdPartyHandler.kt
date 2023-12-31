package com.supertokens.ktor.recipes.thirdparty

import com.supertokens.ktor.recipes.session.sessions
import com.supertokens.ktor.recipes.session.sessionsEnabled
import com.supertokens.ktor.userHandler
import com.supertokens.ktor.utils.BadRequestException
import com.supertokens.ktor.utils.NotFoundException
import com.supertokens.ktor.utils.fronend
import com.supertokens.ktor.utils.setSessionInResponse
import com.supertokens.sdk.common.ThirdPartyProvider
import com.supertokens.sdk.common.requests.ThirdPartySignInUpRequest
import com.supertokens.sdk.common.responses.AuthorizationUrlResponse
import com.supertokens.sdk.common.responses.SignInUpResponse
import com.supertokens.sdk.recipes.thirdparty.providers.Provider
import com.supertokens.sdk.recipes.thirdparty.providers.ThirdPartyUserInfo
import io.ktor.http.URLProtocol
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

    open suspend fun PipelineContext<Unit, ApplicationCall>.handleMissingEmail(provider: Provider<*>, userInfo: ThirdPartyUserInfo): String {
        return "${userInfo.id}@${provider.id}.temp"
    }

    /**
     * A call to POST /signinup
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/signInUp">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.signInUp() {
        val body = call.receive<ThirdPartySignInUpRequest>()
        val provider = body.clientId?.let { thirdParty.getProviderByClientId(it) }
            ?: thirdParty.getProviderById(body.thirdPartyId)
            ?: throw NotFoundException()

        val tokens = body.code?.let {
            provider.getTokens(it, body.redirectURI)
        } ?: body.authCodeResponse ?: throw BadRequestException()

        val userInfo = provider.getUserInfo(tokens)

        val response = thirdParty.signInUp(body.thirdPartyId, userInfo.id, userInfo.email?.id ?: handleMissingEmail(provider, userInfo))

        with(userHandler) {
            if (response.createdNewUser) {
                onUserSignedUp(response.user)
            } else {
                onUserSignedIn(response.user)
            }
        }

        if (sessionsEnabled) {
            val session = sessions.createSession(
                userId = response.user.id,
                userDataInJWT = sessions.getJwtData(response.user),
            )

            setSessionInResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                antiCsrfToken = session.antiCsrfToken,
            )
        }

        body.redirectURI?.let {
            call.respondRedirect(it)
        } ?: call.respond(
            SignInUpResponse(
                user = response.user,
                createdNewUser = response.createdNewUser,
            )
        )
    }

    /**
     * A call to GET /authorisationurl
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/authorisationUrl">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.getAuthorizationUrl() {
        val thirdPartyId = call.parameters["thirdPartyId"] ?: throw NotFoundException()

        val provider = thirdParty.getProviderById(thirdPartyId) ?: throw NotFoundException()

        call.respond(
            AuthorizationUrlResponse(
                url = provider.getAuthorizationEndpoint().fullUrl,
            )
        )
    }

    /**
     * A call to POST /callback/apple
     * @see <a href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/ThirdParty%20Recipe/thirdPartyCallbackApple">Frontend Driver Interface</a>
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.appleAuthCallback() {
        val provider = thirdParty.getProviderById(ThirdPartyProvider.APPLE) ?: throw NotFoundException()
        val formParameters = call.receiveParameters()
        val code = formParameters["code"] ?: throw BadRequestException(message = "Form Param 'code' is required")
        val state = formParameters["state"]

        val tokens = provider.getTokens(code, null)
        val userInfo = provider.getUserInfo(tokens)

        val response = thirdParty.signInUp(provider.id, userInfo.id, userInfo.email?.id ?: handleMissingEmail(provider, userInfo))

        if (sessionsEnabled) {
            val session = sessions.createSession(
                userId = response.user.id,
                userDataInJWT = sessions.getJwtData(response.user),
            )

            setSessionInResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                antiCsrfToken = session.antiCsrfToken,
            )
        }

        call.respondRedirect {
            protocol = URLProtocol.HTTPS
            host = call.fronend.host
        }
    }

}