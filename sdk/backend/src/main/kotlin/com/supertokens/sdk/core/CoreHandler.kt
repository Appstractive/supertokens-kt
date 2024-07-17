package com.supertokens.sdk.core

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.core.requests.CreateJwtRequest
import com.supertokens.sdk.core.requests.DeleteUserRequest
import com.supertokens.sdk.core.responses.CreateJwtResponseDTO
import com.supertokens.sdk.core.responses.GetUsersResponseDTO
import com.supertokens.sdk.get
import com.supertokens.sdk.post
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

internal class CoreHandler {

  suspend fun SuperTokens.getUserById(userId: String): User {
    val response =
        get(
            PATH_GET_USER_BY_ID,
            tenantId = null,
            queryParams =
                mapOf(
                    "userId" to userId,
                ),
        )

    return response.parseUser()
  }

  suspend fun SuperTokens.getUsersByEMail(email: String, tenantId: String?): List<User> {
    val response =
        get(
            PATH_GET_USER_BY_ACCOUNT_INFO,
            tenantId = tenantId,
            queryParams =
                mapOf(
                    "email" to email,
                    "doUnionOfAccountInfo" to (tenantId == null).toString(),
                ))

    return response.parse<GetUsersResponseDTO, List<User>> { requireNotNull(it.users) }
  }

  suspend fun SuperTokens.getUsersByPhoneNumber(
      phoneNumber: String,
      tenantId: String?
  ): List<User> {
    val response =
        get(
            PATH_GET_USER_BY_ACCOUNT_INFO,
            tenantId = tenantId,
            queryParams =
                mapOf(
                    "phoneNumber" to phoneNumber,
                    "doUnionOfAccountInfo" to (tenantId == null).toString(),
                ),
        )

    return response.parse<GetUsersResponseDTO, List<User>> { requireNotNull(it.users) }
  }

  suspend fun SuperTokens.getUsersByThirdParty(
      thirdPartyId: String,
      thirdPartyUserId: String,
      tenantId: String?
  ): List<User> {
    val response =
        get(
            PATH_GET_USER_BY_ACCOUNT_INFO,
            tenantId = tenantId,
            queryParams =
                mapOf(
                    "thirdPartyId" to thirdPartyId,
                    "thirdPartyUserId" to thirdPartyUserId,
                    "doUnionOfAccountInfo" to (tenantId == null).toString(),
                ),
        )

    return response.parse<GetUsersResponseDTO, List<User>> { requireNotNull(it.users) }
  }

  suspend fun SuperTokens.deleteUser(
      userId: String,
      removeAllLinkedAccounts: Boolean
  ): SuperTokensStatus {
    val response =
        post(PATH_DELETE_USER, tenantId = null) {
          setBody(
              DeleteUserRequest(
                  userId = userId,
                  removeAllLinkedAccounts = removeAllLinkedAccounts,
              ))
        }

    return response.parse<StatusResponseDTO, SuperTokensStatus> { it.status.toStatus() }
  }

  suspend fun SuperTokens.getJwks(tenantId: String?): JsonObject {
    val response = get(PATH_JWKS, tenantId = tenantId)

    return response.body()
  }

  suspend fun SuperTokens.createJwt(
      issuer: String,
      validityInSeconds: Long = 86400L,
      useStaticSigningKey: Boolean = false,
      payload: Map<String, Any?>? = null
  ): String {
    val response =
        post(PATH_CREATE_JWT, tenantId = null) {
          setBody(
              CreateJwtRequest(
                  jwksDomain = issuer,
                  validity = validityInSeconds,
                  useStaticSigningKey = useStaticSigningKey,
                  payload = payload?.toJsonElement() ?: JsonObject(emptyMap())))
        }

    return response.parse<CreateJwtResponseDTO, String> { checkNotNull(it.jwt) }
  }

  companion object {
    const val PATH_GET_USER_BY_ID = "/user/id"
    const val PATH_GET_USER_BY_ACCOUNT_INFO = "/users/by-accountinfo"
    const val PATH_DELETE_USER = "/user/remove"
    const val PATH_CREATE_JWT = "/recipe/jwt"
    const val PATH_JWKS = "/.well-known/jwks.json"
  }
}

suspend fun SuperTokens.getUserById(userId: String): User =
    with(core) {
      return getUserById(userId = userId)
    }

suspend fun SuperTokens.getUserByIdOrNull(userId: String): User? =
    runCatching { getUserById(userId = userId) }.getOrNull()

suspend fun SuperTokens.getUsersByEMail(
    email: String,
    tenantId: String? = null,
) =
    with(core) {
      getUsersByEMail(
          email = email,
          tenantId = tenantId,
      )
    }

suspend fun SuperTokens.getUserByEMailOrNull(
    email: String,
    tenantId: String? = null,
): User? =
    runCatching {
          getUsersByEMail(
                  email = email,
                  tenantId = tenantId,
              )
              .firstOrNull()
        }
        .getOrNull()

suspend fun SuperTokens.getUsersByPhoneNumber(
    phoneNumber: String,
    tenantId: String? = null,
) =
    with(core) {
      getUsersByPhoneNumber(
          phoneNumber = phoneNumber,
          tenantId = tenantId,
      )
    }

suspend fun SuperTokens.getUsersByThirdParty(
    thirdPartyId: String,
    thirdPartyUserId: String,
    tenantId: String? = null,
) =
    with(core) {
      getUsersByThirdParty(
          thirdPartyId = thirdPartyId,
          thirdPartyUserId = thirdPartyUserId,
          tenantId = tenantId,
      )
    }

suspend fun SuperTokens.getUsersByThirdPartyOrNull(
    thirdPartyId: String,
    thirdPartyUserId: String,
    tenantId: String? = null,
): User? =
    runCatching {
          getUsersByThirdParty(
                  thirdPartyId = thirdPartyId,
                  thirdPartyUserId = thirdPartyUserId,
                  tenantId = tenantId,
              )
              .firstOrNull()
        }
        .getOrNull()

suspend fun SuperTokens.getUserByPhoneNumberOrNull(
    phoneNumber: String,
    tenantId: String? = null,
): User? =
    runCatching {
          getUsersByPhoneNumber(
                  phoneNumber = phoneNumber,
                  tenantId = tenantId,
              )
              .firstOrNull()
        }
        .getOrNull()

suspend fun SuperTokens.deleteUser(
    userId: String,
    removeAllLinkedAccounts: Boolean = false,
): SuperTokensStatus =
    with(core) {
      return deleteUser(
          userId = userId,
          removeAllLinkedAccounts = removeAllLinkedAccounts,
      )
    }

suspend fun SuperTokens.getJwks(
    tenantId: String? = null,
): JsonObject =
    with(core) {
      return getJwks(
          tenantId = tenantId,
      )
    }

suspend fun SuperTokens.createJwt(
    issuer: String,
    validityInSeconds: Long = 86400L,
    useStaticSigningKey: Boolean = false,
    payload: Map<String, Any?>? = null
): String =
    with(core) {
      return createJwt(
          issuer = issuer,
          validityInSeconds = validityInSeconds,
          useStaticSigningKey = useStaticSigningKey,
          payload = payload,
      )
    }
