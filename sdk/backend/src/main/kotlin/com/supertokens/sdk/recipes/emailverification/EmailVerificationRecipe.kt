package com.supertokens.sdk.recipes.emailverification

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.Claims
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_EMAIL_VERIFICATION
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.AuthFactor
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.common.requests.VerifyEmailTokenRequestDTO
import com.supertokens.sdk.common.responses.StatusResponseDTO
import com.supertokens.sdk.common.responses.VerifyEmailResponseDTO
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.get
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.post
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.emailverification.models.VerifyEmailTokenData
import com.supertokens.sdk.recipes.emailverification.requests.EmailVerificationRequest
import com.supertokens.sdk.recipes.emailverification.responses.CreateEmailVerificationTokenResponseDTO
import com.supertokens.sdk.recipes.emailverification.responses.VerifyEmailTokenResponseDTO
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.header
import io.ktor.client.request.setBody

class EmailVerificationRecipeConfig : RecipeConfig {

  // The email service to use when sending verification mails
  var emailService: EmailService? = null
}

class EmailVerificationRecipe(
    private val superTokens: SuperTokens,
    config: EmailVerificationRecipeConfig
) : Recipe<EmailVerificationRecipeConfig> {

  val emailService: EmailService? = config.emailService

  /** Set the users email to verified */
  suspend fun setVerified(userId: String, email: String, tenantId: String?) {
    val token = createVerificationToken(userId = userId, email = email, tenantId = tenantId)
    verifyToken(token = token)
  }

  /**
   * Check if the email is verified for the user (users without an email are verified (to have
   * none))
   */
  suspend fun isVerified(userId: String, email: String?): Boolean {
    return email?.let {
      val response = runCatching { checkEmailVerified(userId, it) }
      response.getOrElse { true }
      // users without an email are verified (to have none)
    } ?: true
  }

  override suspend fun getExtraJwtData(
      user: User,
      tenantId: String?,
      recipeId: String,
      multiAuthFactor: AuthFactor?,
      accessToken: String?
  ): Map<String, Any?> {
    return buildMap { set(Claims.EMAIL_VERIFIED, isVerified(user.id, user.email)) }
  }

  /** Generate a new email verification token for this user */
  @Throws(SuperTokensStatusException::class)
  suspend fun createVerificationToken(userId: String, email: String, tenantId: String?): String {
    val response =
        superTokens.post(PATH_CREATE_TOKEN, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_VERIFICATION)

          setBody(
              EmailVerificationRequest(
                  userId = userId,
                  email = email,
              ))
        }

    return response.parse<CreateEmailVerificationTokenResponseDTO, String> {
      it.token ?: throw SuperTokensStatusException(SuperTokensStatus.EmailAlreadyVerifiedError)
    }
  }

  /** Remove all unused email verification tokens for this user */
  @Throws(SuperTokensStatusException::class)
  suspend fun removeAllVerificationTokens(userId: String, email: String): SuperTokensStatus {
    val response =
        superTokens.post(PATH_DELETE_TOKENS, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_VERIFICATION)

          setBody(
              EmailVerificationRequest(
                  userId = userId,
                  email = email,
              ))
        }

    return response.parse<StatusResponseDTO, SuperTokensStatus> { it.status.toStatus() }
  }

  /** Verify an email */
  @Throws(SuperTokensStatusException::class)
  suspend fun verifyToken(token: String): VerifyEmailTokenData {
    val response =
        superTokens.post(PATH_VERIFY, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_VERIFICATION)

          setBody(
              VerifyEmailTokenRequestDTO(
                  token = token,
              ))
        }

    return response
        .parse<VerifyEmailTokenResponseDTO, VerifyEmailTokenData> {
          VerifyEmailTokenData(
              userId = checkNotNull(it.userId),
              email = checkNotNull(it.email),
          )
        }
        .also {
          superTokens._events.tryEmit(SuperTokensEvent.UserEmailVerified(it.userId, it.email))
        }
  }

  /** Check if an email is verified */
  @Throws(SuperTokensStatusException::class)
  suspend fun checkEmailVerified(userId: String, email: String): Boolean {
    val response =
        superTokens.get(
            PATH_VERIFY,
            tenantId = null,
            queryParams =
                mapOf(
                    "userId" to userId,
                    "email" to email,
                )) {
              header(HEADER_RECIPE_ID, RECIPE_EMAIL_VERIFICATION)
            }

    return response.parse<VerifyEmailResponseDTO, Boolean> { it.isVerified == true }
  }

  /** Unverify an email */
  @Throws(SuperTokensStatusException::class)
  suspend fun setUnverified(userId: String, email: String): SuperTokensStatus {
    val response =
        superTokens.post(PATH_VERIFY_REMOVE, tenantId = null) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_VERIFICATION)

          setBody(
              EmailVerificationRequest(
                  userId = userId,
                  email = email,
              ))
        }

    return response
        .parse<StatusResponseDTO, SuperTokensStatus> { it.status.toStatus() }
        .also { superTokens._events.tryEmit(SuperTokensEvent.UserEmailUnVerified(userId, email)) }
  }

  companion object {
    const val PATH_CREATE_TOKEN = "/recipe/user/email/verify/token"
    const val PATH_DELETE_TOKENS = "/recipe/user/email/verify/token/remove"
    const val PATH_VERIFY = "/recipe/user/email/verify"
    const val PATH_VERIFY_REMOVE = "/recipe/user/email/verify/remove"
  }
}

val EmailVerification =
    object : RecipeBuilder<EmailVerificationRecipeConfig, EmailVerificationRecipe>() {

      override fun install(
          configure: EmailVerificationRecipeConfig.() -> Unit
      ): (SuperTokens) -> EmailVerificationRecipe {
        val config = EmailVerificationRecipeConfig().apply(configure)

        return { EmailVerificationRecipe(it, config) }
      }
    }

/** Generate a new email verification token for this user */
suspend fun SuperTokens.createEmailVerificationToken(
    userId: String,
    email: String,
    tenantId: String? = null,
) =
    getRecipe<EmailVerificationRecipe>()
        .createVerificationToken(userId = userId, email = email, tenantId = tenantId)

/** Remove all unused email verification tokens for this user */
suspend fun SuperTokens.removeAllVerificationTokens(
    userId: String,
    email: String,
) =
    getRecipe<EmailVerificationRecipe>()
        .removeAllVerificationTokens(
            userId = userId,
            email = email,
        )

/** Verify an email */
suspend fun SuperTokens.verifyToken(
    token: String,
) =
    getRecipe<EmailVerificationRecipe>()
        .verifyToken(
            token = token,
        )

/** Check if an email is verified */
suspend fun SuperTokens.checkEmailVerified(
    userId: String,
    email: String,
) =
    getRecipe<EmailVerificationRecipe>()
        .checkEmailVerified(
            userId = userId,
            email = email,
        )

/** Unverify an email */
suspend fun SuperTokens.setUnverified(
    userId: String,
    email: String,
) =
    getRecipe<EmailVerificationRecipe>()
        .setUnverified(
            userId = userId,
            email = email,
        )
