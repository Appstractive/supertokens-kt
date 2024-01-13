package com.supertokens.sdk.recipes.passwordless

import com.supertokens.sdk.SuperTokensClient
import com.supertokens.sdk.common.RECIPE_PASSWORDLESS
import com.supertokens.sdk.common.models.PasswordlessMode
import com.supertokens.sdk.handlers.SignInProvider
import com.supertokens.sdk.handlers.SignInProviderConfig
import com.supertokens.sdk.handlers.SignUpProvider
import com.supertokens.sdk.handlers.SignUpProviderConfig
import com.supertokens.sdk.models.SignInData
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.core.usecases.CheckEmailExistsUseCase
import com.supertokens.sdk.recipes.passwordless.usecases.CheckPhoneNumberExistsUseCase
import com.supertokens.sdk.recipes.passwordless.usecases.PasswordlessEmailSignUpUseCase
import com.supertokens.sdk.recipes.passwordless.usecases.PasswordlessInputCodeSignInUseCase
import com.supertokens.sdk.recipes.passwordless.usecases.PasswordlessLinkCodeSignInUseCase
import com.supertokens.sdk.recipes.passwordless.usecases.PasswordlessPhoneNumberSignUpUseCase

class PasswordlessConfig: RecipeConfig

class PasswordlessRecipe(
    private val superTokens: SuperTokensClient,
    private val config: PasswordlessConfig,
) : Recipe<PasswordlessConfig> {

    private val passwordlessEmailSignUpUseCase by lazy {
        PasswordlessEmailSignUpUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
        )
    }

    private val passwordlessPhoneNumberSignUpUseCase by lazy {
        PasswordlessPhoneNumberSignUpUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
        )
    }

    private val passwordlessLinkCodeSignInUseCase by lazy {
        PasswordlessLinkCodeSignInUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
        )
    }

    private val passwordlessInputCodeSignInUseCase by lazy {
        PasswordlessInputCodeSignInUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
        )
    }

    private val checkEmailExistsUseCase by lazy {
        CheckEmailExistsUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
            recipeId = RECIPE_PASSWORDLESS,
        )
    }

    private val checkPhoneNumberExistsUseCase by lazy {
        CheckPhoneNumberExistsUseCase(
            client = superTokens.apiClient,
            tenantId = superTokens.tenantId,
        )
    }

    suspend fun signUpEmail(email: String) = passwordlessEmailSignUpUseCase.signUp(email)
    suspend fun signUpPhoneNumber(phoneNumber: String) = passwordlessPhoneNumberSignUpUseCase.signUp(phoneNumber)
    suspend fun signInLinkCode(preAuthSessionId: String, linkCode: String) = passwordlessLinkCodeSignInUseCase.signIn(
        preAuthSessionId = preAuthSessionId,
        linkCode = linkCode,
    )
    suspend fun signInInputCode(preAuthSessionId: String, deviceId: String, userInputCode: String) = passwordlessInputCodeSignInUseCase.signIn(
        preAuthSessionId = preAuthSessionId,
        deviceId = deviceId,
        userInputCode = userInputCode,
    )

    suspend fun checkEmailExists(email: String) = checkEmailExistsUseCase.checkEmailExists(email)
    suspend fun checkPhoneNumberExists(phoneNumber: String) = checkPhoneNumberExistsUseCase.checkPhoneNumberExists(phoneNumber)

}

data class PasswordlessSignUpData(
    val deviceId: String,
    val preAuthSessionId: String,
    val flowType: PasswordlessMode,
)

object Passwordless : RecipeBuilder<PasswordlessConfig, PasswordlessRecipe>(), SignUpProvider<Passwordless.SignUpConfig, PasswordlessSignUpData> {

    override fun install(configure: PasswordlessConfig.() -> Unit): (SuperTokensClient) -> PasswordlessRecipe {
        val config = PasswordlessConfig().apply(configure)

        return {
            PasswordlessRecipe(it, config)
        }
    }

    data class SignUpConfig(
        var email: String? = null,
        var phoneNumber: String? = null
    ) : SignUpProviderConfig

    override suspend fun signUp(superTokensClient: SuperTokensClient, configure: SignUpConfig.() -> Unit): PasswordlessSignUpData {
        val config = SignUpConfig().apply(configure)

        return config.email?.let {
            superTokensClient.getRecipe<PasswordlessRecipe>().signUpEmail(it)
        } ?: config.phoneNumber?.let {
            superTokensClient.getRecipe<PasswordlessRecipe>().signUpPhoneNumber(it)
        } ?: throw IllegalStateException("Either 'email' or 'phoneNumber' must be provided")
    }

}

object PasswordlessLinkCode : SignInProvider<PasswordlessLinkCode.SignInConfig, SignInData> {

    data class SignInConfig(
        var preAuthSessionId: String? = null,
        var linkCode: String? = null,
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: SignInConfig.() -> Unit): SignInData {
        val config = SignInConfig().apply(configure)

        val preAuthSessionId = config.preAuthSessionId
        val linkCode = config.linkCode

        if(preAuthSessionId == null || linkCode == null) {
            throw IllegalStateException("'preAuthSessionId' and 'linkCode' must be provided")
        }

        return superTokensClient.getRecipe<PasswordlessRecipe>().signInLinkCode(
            preAuthSessionId = preAuthSessionId,
            linkCode = linkCode,
        )
    }

}

object PasswordlessInputCode : SignInProvider<PasswordlessInputCode.SignInConfig, SignInData> {

    data class SignInConfig(
        var preAuthSessionId: String? = null,
        var deviceId: String? = null,
        var userInputCode: String? = null,
    ) : SignInProviderConfig

    override suspend fun signIn(superTokensClient: SuperTokensClient, configure: SignInConfig.() -> Unit): SignInData {
        val config = SignInConfig().apply(configure)

        val preAuthSessionId = config.preAuthSessionId
        val deviceId = config.deviceId
        val userInputCode = config.userInputCode

        if(preAuthSessionId == null || deviceId == null || userInputCode == null) {
            throw IllegalStateException("'preAuthSessionId', 'deviceId' and 'userInputCode' must be provided")
        }

        return superTokensClient.getRecipe<PasswordlessRecipe>().signInInputCode(
            preAuthSessionId = preAuthSessionId,
            deviceId = deviceId,
            userInputCode = userInputCode,
        )
    }
}

suspend fun SuperTokensClient.checkPhoneNumberExists(phoneNumber: String): Boolean {
    return getRecipe<PasswordlessRecipe>().checkPhoneNumberExists(phoneNumber)
}