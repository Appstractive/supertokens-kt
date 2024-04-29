package com.supertokens.sdk.recipes.accountlinking

import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.HEADER_RECIPE_ID
import com.supertokens.sdk.common.RECIPE_ACCOUNT_LINKING
import com.supertokens.sdk.common.RECIPE_EMAIL_PASSWORD
import com.supertokens.sdk.common.RECIPE_EMAIL_VERIFICATION
import com.supertokens.sdk.common.SuperTokensStatusException
import com.supertokens.sdk.common.models.User
import com.supertokens.sdk.core.getUserByIdOrNull
import com.supertokens.sdk.get
import com.supertokens.sdk.models.SuperTokensEvent
import com.supertokens.sdk.post
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.accountlinking.models.CreatePrimaryUserData
import com.supertokens.sdk.recipes.accountlinking.models.LinkAccountsData
import com.supertokens.sdk.recipes.accountlinking.models.UnlinkAccountsData
import com.supertokens.sdk.recipes.accountlinking.requests.CreatePrimaryUserRequestDTO
import com.supertokens.sdk.recipes.accountlinking.requests.LinkAccountsRequestDTO
import com.supertokens.sdk.recipes.accountlinking.requests.UnlinkAccountsRequestDTO
import com.supertokens.sdk.recipes.accountlinking.responses.CheckCanLinkAccountsResponseDTO
import com.supertokens.sdk.recipes.accountlinking.responses.CheckPrimaryUserResponseDTO
import com.supertokens.sdk.recipes.accountlinking.responses.CreatePrimaryUserResponseDTO
import com.supertokens.sdk.recipes.accountlinking.responses.LinkAccountsResponseDTO
import com.supertokens.sdk.recipes.accountlinking.responses.UnlinkAccountsResponseDTO
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

typealias ShouldDoAccountLinking = suspend (SuperTokens, User) -> ShouldDoAccountLinkingResult

data class ShouldDoAccountLinkingResult(
    val shouldAutomaticallyLink: Boolean = false,
    val shouldRequireVerification: Boolean = true,
)

class AccountLinkingRecipeConfig : RecipeConfig {
    var shouldDoAutomaticAccountLinking: ShouldDoAccountLinking = { _, _ -> ShouldDoAccountLinkingResult() }

    var scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}

class AccountLinkingRecipe(
    private val superTokens: SuperTokens,
    private val config: AccountLinkingRecipeConfig
) : Recipe<AccountLinkingRecipeConfig> {

    init {
        config.scope.launch {
            superTokens.events.collect {
                when (it) {
                    is SuperTokensEvent.UserEmailVerified -> tryLinkAccounts(
                        userId = it.userId,
                        recipeId = RECIPE_EMAIL_VERIFICATION,
                    )

                    is SuperTokensEvent.UserSignIn -> tryLinkAccounts(
                        user = it.user,
                        recipeId = it.recipeId,
                    )

                    is SuperTokensEvent.UserSignUp -> tryLinkAccounts(
                        user = it.user,
                        recipeId = it.recipeId,
                    )

                    else -> {}
                }
            }
        }
    }

    private suspend fun tryLinkAccounts(userId: String, recipeId: String) {
        when (recipeId) {
            RECIPE_EMAIL_VERIFICATION -> superTokens.getUserByIdOrNull(userId = userId)?.let {
                tryLinkAccounts(user = it, recipeId = RECIPE_EMAIL_PASSWORD)
            }
        }
    }

    private suspend fun tryLinkAccounts(user: User, recipeId: String) {
        runCatching {
            // TODO verify handling
            val result = config.shouldDoAutomaticAccountLinking(superTokens, user)

            if (result.shouldAutomaticallyLink) {
                val primaryUser = if (!user.isPrimaryUser) {
                    superTokens.createPrimaryUser(recipeUserId = user.recipeUserId ?: user.id).user
                } else {
                    user
                }

                user.loginMethods?.forEach { loginMethod ->
                    val isVerified = !result.shouldRequireVerification || loginMethod.verified
                    val isSameEMail = primaryUser.emails?.any { it == loginMethod.email } == true
                    val isSamePhoneNumber = primaryUser.phoneNumbers?.any { it == loginMethod.phoneNumber } == true
                    if (isVerified && (isSameEMail || isSamePhoneNumber)) {
                        superTokens.linkAccounts(
                            primaryUserId = primaryUser.id,
                            recipeUserId = loginMethod.recipeUserId,
                        )
                    }
                }
            }
        }
    }

    /**
     *  Check if primary user can be created for given user id
     *  @return true, if wasAlreadyAPrimaryUser is true
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun checkCanCreatePrimaryUser(recipeUserId: String): Boolean {
        val response = superTokens.get(
            PATH_PRIMARY_USER_CHECK,
            tenantId = null,
            queryParams = mapOf(
                "recipeUserId" to recipeUserId,
            ),
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ACCOUNT_LINKING)
        }

        return response.parse<CheckPrimaryUserResponseDTO, Boolean> {
            it.wasAlreadyAPrimaryUser
        }
    }

    /**
     *  Check if accounts can be linked for given primary and recipe user id
     *  @return true, if accountsAlreadyLinked is true
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun checkCanLinkAccounts(primaryUserId: String, recipeUserId: String): Boolean {
        val response = superTokens.get(
            PATH_CAN_LINK_CHECK,
            tenantId = null,
            queryParams = mapOf(
                "primaryUserId" to primaryUserId,
                "recipeUserId" to recipeUserId,
            ),
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ACCOUNT_LINKING)
        }

        return response.parse<CheckCanLinkAccountsResponseDTO, Boolean> {
            it.accountsAlreadyLinked
        }
    }

    /**
     *  Create a primary user for given user id
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun createPrimaryUser(recipeUserId: String): CreatePrimaryUserData {
        val response = superTokens.post(
            PATH_PRIMARY_USER_CREATE,
            tenantId = null,
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ACCOUNT_LINKING)

            setBody(
                CreatePrimaryUserRequestDTO(
                    recipeUserId = recipeUserId,
                )
            )
        }

        return response.parse<CreatePrimaryUserResponseDTO, CreatePrimaryUserData> {
            CreatePrimaryUserData(
                wasAlreadyAPrimaryUser = it.wasAlreadyAPrimaryUser,
                user = it.user,
            )
        }
    }

    /**
     *  Link accounts for given primary and recipe user id
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun linkAccounts(primaryUserId: String, recipeUserId: String): LinkAccountsData {
        val response = superTokens.post(
            PATH_ACCOUNTS_LINK,
            tenantId = null,
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ACCOUNT_LINKING)

            setBody(
                LinkAccountsRequestDTO(
                    primaryUserId = primaryUserId,
                    recipeUserId = recipeUserId,
                )
            )
        }

        return response.parse<LinkAccountsResponseDTO, LinkAccountsData> {
            LinkAccountsData(
                accountsAlreadyLinked = it.accountsAlreadyLinked,
                user = it.user,
            )
        }
    }

    /**
     *  Unlink accounts for given recipe user id
     */
    @Throws(SuperTokensStatusException::class)
    suspend fun unlinkAccounts(recipeUserId: String): UnlinkAccountsData {
        val response = superTokens.post(
            PATH_ACCOUNTS_UNLINK,
            tenantId = null,
        ) {
            header(HEADER_RECIPE_ID, RECIPE_ACCOUNT_LINKING)

            setBody(
                UnlinkAccountsRequestDTO(
                    recipeUserId = recipeUserId,
                )
            )
        }

        return response.parse<UnlinkAccountsResponseDTO, UnlinkAccountsData> {
            UnlinkAccountsData(
                wasRecipeUserDeleted = it.wasRecipeUserDeleted,
                wasLinked = it.wasLinked,
            )
        }
    }

    companion object {
        const val PATH_PRIMARY_USER_CREATE = "/recipe/accountlinking/user/primary"
        const val PATH_PRIMARY_USER_CHECK = "/recipe/accountlinking/user/primary/check"
        const val PATH_CAN_LINK_CHECK = "/recipe/accountlinking/user/link/check"
        const val PATH_ACCOUNTS_LINK = "/recipe/accountlinking/user/link"
        const val PATH_ACCOUNTS_UNLINK = "/recipe/accountlinking/user/unlink"
    }
}

val AccountLinking = object : RecipeBuilder<AccountLinkingRecipeConfig, AccountLinkingRecipe>() {

    override fun install(configure: AccountLinkingRecipeConfig.() -> Unit): (SuperTokens) -> AccountLinkingRecipe {
        val config = AccountLinkingRecipeConfig().apply(configure)

        return {
            AccountLinkingRecipe(it, config)
        }
    }
}

/**
 *  Check if primary user can be created for given user id
 *  @return true, if wasAlreadyAPrimaryUser is true
 */
@Throws(SuperTokensStatusException::class)
suspend fun SuperTokens.checkCanCreatePrimaryUser(
    recipeUserId: String,
) = getRecipe<AccountLinkingRecipe>().checkCanCreatePrimaryUser(recipeUserId = recipeUserId)

/**
 * Check if accounts can be linked for given primary and recipe user id
 * @return true, if accountsAlreadyLinked is true
 */
@Throws(SuperTokensStatusException::class)
suspend fun SuperTokens.checkCanLinkAccounts(
    primaryUserId: String,
    recipeUserId: String,
) = getRecipe<AccountLinkingRecipe>().checkCanLinkAccounts(
    primaryUserId = primaryUserId,
    recipeUserId = recipeUserId,
)

/**
 *  Create a primary user for given user id
 */
@Throws(SuperTokensStatusException::class)
suspend fun SuperTokens.createPrimaryUser(
    recipeUserId: String,
) = getRecipe<AccountLinkingRecipe>().createPrimaryUser(recipeUserId = recipeUserId)

/**
 * Link accounts for given primary and recipe user id
 */
@Throws(SuperTokensStatusException::class)
suspend fun SuperTokens.linkAccounts(
    primaryUserId: String,
    recipeUserId: String,
) = getRecipe<AccountLinkingRecipe>().linkAccounts(
    primaryUserId = primaryUserId,
    recipeUserId = recipeUserId,
)

/**
 * Unlink accounts for given recipe user id
 */
@Throws(SuperTokensStatusException::class)
suspend fun SuperTokens.unlinkAccounts(
    recipeUserId: String,
) = getRecipe<AccountLinkingRecipe>().unlinkAccounts(recipeUserId = recipeUserId)
