package com.supertokens.sdk.recipes.passwordless

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.models.SignInUpData
import com.supertokens.sdk.recipes.passwordless.models.PasswordlessCodeData
import com.supertokens.sdk.common.requests.ConsumePasswordlessCodeRequest
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.recipes.passwordless.requests.CreatePasswordlessCodeRequest
import com.supertokens.sdk.recipes.passwordless.requests.RevokeAllPasswordlessCodesRequest
import com.supertokens.sdk.recipes.passwordless.requests.RevokePasswordlesCodeRequest
import com.supertokens.sdk.recipes.passwordless.responses.ConsumePasswordlessCodeResponse
import com.supertokens.sdk.recipes.passwordless.responses.GetPasswordlessCodesResponse
import com.supertokens.sdk.recipes.passwordless.responses.PasswordlessCodeResponse
import com.supertokens.sdk.recipes.passwordless.responses.PasswordlessDevices
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class PasswordlessRecipeConfig : RecipeConfig {

    var mode: PasswordlessMode = PasswordlessMode.MAGIC_LINK

    var emailService: EmailService? = null

}

class PasswordlessRecipe(
    private val superTokens: SuperTokens,
    private val config: PasswordlessRecipeConfig
) : Recipe<PasswordlessRecipeConfig> {

    val flowType: PasswordlessMode = config.mode

    val emailService: EmailService? = config.emailService

    suspend fun createEmailCode(email: String): PasswordlessCodeData {
        val response = superTokens.client.post(PATH_CREATE_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    email = email,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    suspend fun createPhoneNumberCode(phoneNumber: String): PasswordlessCodeData {
        val response = superTokens.client.post(PATH_CREATE_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    phoneNumber = phoneNumber,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    suspend fun recreateCode(deviceId: String): PasswordlessCodeData {
        val response = superTokens.client.post(PATH_CREATE_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                CreatePasswordlessCodeRequest(
                    deviceId = deviceId,
                )
            )
        }

        return response.parse<PasswordlessCodeResponse, PasswordlessCodeData> {
            PasswordlessCodeData(
                preAuthSessionId = checkNotNull(it.preAuthSessionId),
                codeId = checkNotNull(it.codeId),
                deviceId = checkNotNull(it.deviceId),
                userInputCode = checkNotNull(it.userInputCode),
                linkCode = checkNotNull(it.linkCode),
                timeCreated = checkNotNull(it.timeCreated),
                codeLifetime = checkNotNull(it.codeLifetime),
            )
        }
    }

    suspend fun consumeLinkCode(preAuthSessionId: String, linkCode: String): SignInUpData {
        val response = superTokens.client.post(PATH_CONSUME_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ConsumePasswordlessCodeRequest(
                    preAuthSessionId = preAuthSessionId,
                    linkCode = linkCode,
                )
            )
        }

        return response.parse<ConsumePasswordlessCodeResponse, SignInUpData> {
            SignInUpData(
                user = checkNotNull(it.user),
                createdNewUser = checkNotNull(it.createdNewUser),
            )
        }.also {
            if(it.createdNewUser) {
                superTokens._events.tryEmit(SuperTokensEvent.UserSignUp(it.user))
            }
            else {
                superTokens._events.tryEmit(SuperTokensEvent.UserSignIn(it.user))
            }
        }
    }

    suspend fun consumeUserInputCode(preAuthSessionId: String, deviceId: String, userInputCode: String): SignInUpData {
        val response = superTokens.client.post(PATH_CONSUME_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                ConsumePasswordlessCodeRequest(
                    preAuthSessionId = preAuthSessionId,
                    deviceId = deviceId,
                    userInputCode = userInputCode,
                )
            )
        }

        return response.parse<ConsumePasswordlessCodeResponse, SignInUpData> {
            SignInUpData(
                user = it.user ?: throw RuntimeException("OK StatusResponse without user"),
                createdNewUser = it.createdNewUser ?: throw RuntimeException("OK StatusResponse without createdNewUser"),
            )
        }
    }

    suspend fun revokeCode(codeId: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_REVOKE_CODE) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokePasswordlesCodeRequest(
                    codeId = codeId,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun revokeEmailCodes(email: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_REVOKE_CODES) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokeAllPasswordlessCodesRequest(
                    email = email,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun revokePhoneNumberCodes(phoneNumber: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_REVOKE_CODES) {

            header(Constants.HEADER_RECIPE_ID, ID)

            setBody(
                RevokeAllPasswordlessCodesRequest(
                    phoneNumber = phoneNumber,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    suspend fun getCodesByEmail(email: String): List<PasswordlessDevices> {
        val response = superTokens.client.get("$PATH_GET_CODES?email=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(email, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    suspend fun getCodesByDeviceId(deviceId: String): List<PasswordlessDevices> {
        val response = superTokens.client.get("$PATH_GET_CODES?deviceId=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(deviceId, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    suspend fun getCodesByPhoneNumber(phoneNumber: String): List<PasswordlessDevices> {
        val response = superTokens.client.get("$PATH_GET_CODES?phoneNumber=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(phoneNumber, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    suspend fun getCodesByPreAuthSessionId(preAuthSessionId: String): List<PasswordlessDevices> {
        val response = superTokens.client.get("$PATH_GET_CODES?preAuthSessionId=${
            withContext(Dispatchers.IO) {
                URLEncoder.encode(preAuthSessionId, "UTF-8")
            }
        }") {
            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<GetPasswordlessCodesResponse, List<PasswordlessDevices>> {
            it.devices
        }
    }

    companion object {
        const val ID = "passwordless"

        const val PATH_CREATE_CODE = "/recipe/signinup/code"
        const val PATH_CONSUME_CODE = "/recipe/signinup/code/consume"
        const val PATH_REVOKE_CODE = "/recipe/signinup/code/remove"
        const val PATH_REVOKE_CODES = "/recipe/signinup/codes/remove"
        const val PATH_GET_CODES = "/recipe/signinup/codes"
    }

}

val Passwordless = object : RecipeBuilder<PasswordlessRecipeConfig, PasswordlessRecipe>() {

    override fun install(configure: PasswordlessRecipeConfig.() -> Unit): (SuperTokens) -> PasswordlessRecipe {
        val config = PasswordlessRecipeConfig().apply(configure)

        return {
            PasswordlessRecipe(it, config)
        }
    }

}

suspend fun SuperTokens.createPasswordlessEmailCode(
    email: String,
) = getRecipe<PasswordlessRecipe>().createEmailCode(email)

suspend fun SuperTokens.createPasswordlessPhoneNumberCode(
    phoneNumber: String,
) = getRecipe<PasswordlessRecipe>().createPhoneNumberCode(phoneNumber)

suspend fun SuperTokens.recreatePasswordlessCode(
    deviceId: String,
) = getRecipe<PasswordlessRecipe>().recreateCode(deviceId)

suspend fun SuperTokens.consumePasswordlessLinkCode(
    preAuthSessionId: String,
    linkCode: String,
) = getRecipe<PasswordlessRecipe>().consumeLinkCode(preAuthSessionId, linkCode)

suspend fun SuperTokens.consumePasswordlessUserInputCode(
    preAuthSessionId: String,
    deviceId: String,
    userInputCode: String
) = getRecipe<PasswordlessRecipe>().consumeUserInputCode(preAuthSessionId, deviceId, userInputCode)

suspend fun SuperTokens.revokePasswordlessCode(
    codeId: String,
) = getRecipe<PasswordlessRecipe>().revokeCode(codeId)

suspend fun SuperTokens.revokePasswordlessEmailCodes(
    email: String,
) = getRecipe<PasswordlessRecipe>().revokeEmailCodes(email)

suspend fun SuperTokens.revokePasswordlessPhoneNumberCodes(
    phoneNumber: String,
) = getRecipe<PasswordlessRecipe>().revokePhoneNumberCodes(phoneNumber)

suspend fun SuperTokens.getPasswordlessCodesByEmail(
    email: String,
) = getRecipe<PasswordlessRecipe>().getCodesByEmail(email)

suspend fun SuperTokens.getPasswordlessCodesByDeviceId(
    deviceId: String,
) = getRecipe<PasswordlessRecipe>().getCodesByDeviceId(deviceId)

suspend fun SuperTokens.getPasswordlessCodesByPhoneNumber(
    phoneNumber: String,
) = getRecipe<PasswordlessRecipe>().getCodesByPhoneNumber(phoneNumber)

suspend fun SuperTokens.getPasswordlessCodesByPreAuthSessionId(
    preAuthSessionId: String,
) = getRecipe<PasswordlessRecipe>().getCodesByPreAuthSessionId(preAuthSessionId)