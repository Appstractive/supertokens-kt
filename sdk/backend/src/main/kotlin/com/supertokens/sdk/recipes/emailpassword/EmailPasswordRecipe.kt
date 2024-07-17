package com.supertokens.sdk.recipes.emailpassword

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.FORM_FIELD_EMAIL_ID
import com.supertokens.sdk.common.FORM_FIELD_PASSWORD_ID
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.ingredients.email.EmailService
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.post
import com.supertokens.sdk.put
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.common.models.FormField
import com.supertokens.sdk.recipes.common.models.Validate
import com.supertokens.sdk.recipes.emailpassword.models.ImportUserData
import com.supertokens.sdk.recipes.emailpassword.requests.ConsumePasswordTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.CreateResetPasswordTokenRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignInRequest
import com.supertokens.sdk.recipes.emailpassword.requests.EmailPasswordSignUpRequest
import com.supertokens.sdk.recipes.emailpassword.requests.ImportUserRequest
import com.supertokens.sdk.recipes.emailpassword.requests.UpdateUserRequest
import com.supertokens.sdk.recipes.emailpassword.responses.ConsumePasswordTokenResponseDTO
import com.supertokens.sdk.recipes.emailpassword.responses.CreateResetPasswordTokenResponseDTO
import com.supertokens.sdk.recipes.emailpassword.responses.ImportUserResponseDTO
import com.supertokens.sdk.utils.parse
import com.supertokens.sdk.utils.parseUser
import io.ktor.client.request.header
import io.ktor.client.request.setBody

class EmailPasswordConfig : RecipeConfig {
  // The form fields to parse and validate on signup
  var formFields: List<FormField> = EmailPasswordRecipe.DEFAULT_FORM_FIELDS

  // the service to use when sending emails (password reset)
  var emailService: EmailService? = null

  // if true, validate the password before sending it to core on update
  var validatePasswordOnUpdate: Boolean = true

  // if true, validate the password before sending it to core on reset
  var validatePasswordOnReset: Boolean = true
}

class EmailPasswordRecipe(
    private val superTokens: SuperTokens,
    private val config: EmailPasswordConfig,
) : Recipe<EmailPasswordConfig> {

  val formFields: List<FormField> = config.formFields.toList()
  val emailService: EmailService? = config.emailService

  /** Signup a user with email ID and password */
  @Throws(SuperTokensStatusException::class)
  suspend fun signUp(email: String, password: String, tenantId: String?): User {
    val response =
        superTokens.post(PATH_SIGNUP, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(EmailPasswordSignUpRequest(email = email, password = password))
        }

    return response.parseUser().also {
      superTokens._events.tryEmit(SuperTokensEvent.UserSignUp(it, RECIPE_EMAIL_PASSWORD))
    }
  }

  /** Signin a user with email ID and password */
  @Throws(SuperTokensStatusException::class)
  suspend fun signIn(email: String, password: String, tenantId: String?): User {
    val response =
        superTokens.post(PATH_SIGNIN, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(EmailPasswordSignInRequest(email = email, password = password))
        }

    return response.parseUser().also {
      superTokens._events.tryEmit(SuperTokensEvent.UserSignIn(it, RECIPE_EMAIL_PASSWORD))
    }
  }

  /** Generate a new reset password token for this user */
  @Throws(SuperTokensStatusException::class)
  suspend fun createResetPasswordToken(userId: String, email: String, tenantId: String?): String {
    val response =
        superTokens.post(PATH_CREATE_PASSWORD_RESET_TOKEN, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(
              CreateResetPasswordTokenRequest(
                  userId = userId,
                  email = email,
              ))
        }

    return response.parse<CreateResetPasswordTokenResponseDTO, String> { checkNotNull(it.token) }
  }

  /** Reset a password using password reset token */
  @Throws(SuperTokensStatusException::class)
  suspend fun resetPasswordWithToken(
      token: String,
      newPassword: String,
      tenantId: String?
  ): String {

    if (config.validatePasswordOnReset && !validatePassword(newPassword)) {
      throw SuperTokensStatusException(SuperTokensStatus.PasswordPolicyViolatedError)
    }

    val response =
        superTokens.post(PATH_CONSUME_PASSWORD_RESET_TOKEN, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(
              ConsumePasswordTokenRequest(
                  token = token,
              ))
        }

    val userId = response.parse<ConsumePasswordTokenResponseDTO, String> { checkNotNull(it.userId) }

    updatePassword(
        userId = userId,
        password = newPassword,
        tenantId = tenantId,
    )

    return userId
  }

  /** Update a user's email */
  @Throws(SuperTokensStatusException::class)
  suspend fun updateEmail(userId: String, email: String, tenantId: String?): SuperTokensStatus {
    val response =
        superTokens.put(PATH_UPDATE_USER, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(
              UpdateUserRequest(
                  recipeUserId = userId,
                  email = email,
              ))
        }

    return response.parse().also {
      superTokens._events.tryEmit(SuperTokensEvent.UserEmailChanged(userId, email))
    }
  }

  /** Update a user's password */
  @Throws(SuperTokensStatusException::class)
  suspend fun updatePassword(
      userId: String,
      password: String,
      tenantId: String?
  ): SuperTokensStatus {

    if (config.validatePasswordOnUpdate && !validatePassword(password)) {
      return SuperTokensStatus.PasswordPolicyViolatedError
    }

    val response =
        superTokens.put(PATH_UPDATE_USER, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(
              UpdateUserRequest(
                  recipeUserId = userId,
                  password = password,
              ))
        }

    return response.parse().also {
      superTokens._events.tryEmit(SuperTokensEvent.UserPasswordChanged(userId))
    }
  }

  /** Import a user with email ID and password hash */
  @Throws(SuperTokensStatusException::class)
  suspend fun importUser(
      email: String,
      passwordHash: String,
      hashingAlgorithm: String,
      tenantId: String?
  ): ImportUserData {
    val response =
        superTokens.post(PATH_IMPORT_USER, tenantId = tenantId) {
          header(HEADER_RECIPE_ID, RECIPE_EMAIL_PASSWORD)

          setBody(
              ImportUserRequest(
                  email = email,
                  passwordHash = passwordHash,
                  hashingAlgorithm = hashingAlgorithm,
              ))
        }

    return response.parse<ImportUserResponseDTO, ImportUserData> {
      ImportUserData(
          user = requireNotNull(it.user),
          didUserAlreadyExist = requireNotNull(it.didUserAlreadyExist),
      )
    }
  }

  /** Validate a password against the configured form field */
  @Throws(SuperTokensStatusException::class)
  fun validatePassword(password: String): Boolean {
    formFields
        .firstOrNull { it.id == FORM_FIELD_PASSWORD_ID }
        ?.validate
        ?.let {
          if (!it.invoke(password)) {
            return false
          }
        }

    return true
  }

  companion object {
    const val PATH_SIGNIN = "/recipe/signin"
    const val PATH_SIGNUP = "/recipe/signup"
    const val PATH_UPDATE_USER = "/recipe/user"
    const val PATH_CREATE_PASSWORD_RESET_TOKEN = "/recipe/user/password/reset/token"
    const val PATH_CONSUME_PASSWORD_RESET_TOKEN = "/recipe/user/password/reset/token/consume"
    const val PATH_IMPORT_USER = "/recipe/user/passwordhash/import"

    private val DEFAULT_PASSWORD_REGEXP = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    private val DEFAULT_PASSWORD_VALIDATOR: Validate = { DEFAULT_PASSWORD_REGEXP.matches(it) }

    private val DEFAULT_EMAIL_REGEXP = Regex("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val DEFAULT_EMAIL_VALIDATOR: Validate = { DEFAULT_EMAIL_REGEXP.matches(it) }

    val DEFAULT_FORM_FIELDS =
        listOf(
            FormField(
                id = FORM_FIELD_EMAIL_ID,
                optional = false,
                validate = DEFAULT_EMAIL_VALIDATOR,
            ),
            FormField(
                id = FORM_FIELD_PASSWORD_ID,
                optional = false,
                validate = DEFAULT_PASSWORD_VALIDATOR,
            ),
        )
  }
}

val EmailPassword =
    object : RecipeBuilder<EmailPasswordConfig, EmailPasswordRecipe>() {

      override fun install(
          configure: EmailPasswordConfig.() -> Unit
      ): (SuperTokens) -> EmailPasswordRecipe {
        val config = EmailPasswordConfig().apply(configure)

        return { EmailPasswordRecipe(it, config) }
      }
    }

/** Signup a user with email ID and password */
suspend fun SuperTokens.emailPasswordSignUp(
    email: String,
    password: String,
    tenantId: String? = null
) =
    getRecipe<EmailPasswordRecipe>()
        .signUp(
            email = email,
            password = password,
            tenantId = tenantId,
        )

/** Signin a user with email ID and password */
suspend fun SuperTokens.emailPasswordSignIn(
    email: String,
    password: String,
    tenantId: String? = null
) =
    getRecipe<EmailPasswordRecipe>()
        .signIn(
            email = email,
            password = password,
            tenantId = tenantId,
        )

/** Generate a new reset password token for this user */
suspend fun SuperTokens.createResetPasswordToken(
    userId: String,
    email: String,
    tenantId: String? = null
) =
    getRecipe<EmailPasswordRecipe>()
        .createResetPasswordToken(
            userId = userId,
            email = email,
            tenantId = tenantId,
        )

/** Reset a password using password reset token */
suspend fun SuperTokens.resetPasswordWithToken(
    token: String,
    newPassword: String,
    tenantId: String? = null
) =
    getRecipe<EmailPasswordRecipe>()
        .resetPasswordWithToken(
            token = token,
            newPassword = newPassword,
            tenantId = tenantId,
        )

/** Update a user's email */
suspend fun SuperTokens.updateEmail(userId: String, email: String, tenantId: String? = null) =
    getRecipe<EmailPasswordRecipe>()
        .updateEmail(
            userId = userId,
            email = email,
            tenantId = tenantId,
        )

/** Update a user's password */
suspend fun SuperTokens.updatePassword(userId: String, password: String, tenantId: String? = null) =
    getRecipe<EmailPasswordRecipe>()
        .updatePassword(
            userId = userId,
            password = password,
            tenantId = tenantId,
        )

/** Import a user with email ID and password hash */
suspend fun SuperTokens.importUser(
    email: String,
    passwordHash: String,
    hashingAlgorithm: String,
    tenantId: String? = null
) =
    getRecipe<EmailPasswordRecipe>()
        .importUser(
            email = email,
            passwordHash = passwordHash,
            hashingAlgorithm = hashingAlgorithm,
            tenantId = tenantId,
        )
