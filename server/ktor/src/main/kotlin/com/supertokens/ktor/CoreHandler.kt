package com.supertokens.ktor

import com.supertokens.sdk.common.responses.ExistsResponseDTO
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.core.getUsersByPhoneNumber
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineScope

open class CoreHandler(
    protected val scope: CoroutineScope,
) {

  /**
   * A call to GET /signup/email/exists
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/EmailPassword%20Recipe/emailExists">Frontend
   *   Driver Interface</a>
   */
  open suspend fun PipelineContext<Unit, ApplicationCall>.emailExists() {
    val email = call.parameters["email"] ?: return call.respond(HttpStatusCode.NotFound)

    val response = runCatching { call.superTokens.getUsersByEMail(email) }

    call.respond(
        ExistsResponseDTO(
            exists = response.isSuccess,
        ))
  }

  /**
   * A call to GET /signup/phonenumber/exists
   *
   * @see <a
   *   href="https://app.swaggerhub.com/apis/supertokens/FDI/1.16.0#/Passwordless%20Recipe/passwordlessPhoneNumberExists">Frontend
   *   Driver Interface</a>
   */
  open suspend fun PipelineContext<Unit, ApplicationCall>.phoneNumberExists() {
    val phoneNumber = call.parameters["phoneNumber"] ?: return call.respond(HttpStatusCode.NotFound)

    val response = runCatching { call.superTokens.getUsersByPhoneNumber(phoneNumber) }

    call.respond(
        ExistsResponseDTO(
            exists = response.isSuccess,
        ))
  }
}
