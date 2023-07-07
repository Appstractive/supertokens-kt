package com.supertokens.sdk.recipes.usermetadata

import com.supertokens.sdk.Constants
import com.supertokens.sdk.SuperTokens
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.common.extractedContent
import com.supertokens.sdk.common.responses.StatusResponse
import com.supertokens.sdk.common.toJsonElement
import com.supertokens.sdk.common.toStatus
import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import com.supertokens.sdk.recipes.session.SessionRecipe
import com.supertokens.sdk.recipes.usermetadata.requests.DeleteUserMetaDataRequest
import com.supertokens.sdk.recipes.usermetadata.requests.UpdateUserMetaDataRequest
import com.supertokens.sdk.recipes.usermetadata.responses.UserMetaDataResponse
import com.supertokens.sdk.utils.parse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class UserMetaDataRecipeConfig: RecipeConfig

class UserMetaDataRecipe(
    private val superTokens: SuperTokens,
    private val config: UserMetaDataRecipeConfig
): Recipe<UserMetaDataRecipeConfig> {

    /**
     * Gets the stored metadata object of the user
     */
    suspend fun getMetaData(userId: String): Map<String, Any?> {
        val response = superTokens.client.get("$PATH_META_DATA?userId=$userId") {

            header(Constants.HEADER_RECIPE_ID, ID)
        }

        return response.parse<UserMetaDataResponse, Map<String, Any?>> {
            it.metadata.extractedContent
        }
    }

    /**
     * Updates the metadata object stored about the user by doing a shallow merge of the stored and the update JSONs and removing properties set to null on the root level of the update object. The merged object is then reserialized and stored.
     *  e.g.:
     *  stored: { "preferences": { "theme":"dark" }, "notifications": { "email": true }, "todos": ["example"] }
     *  update: { "notifications": { "sms": true }, "todos": null }
     *  result: { "preferences": { "theme":"dark" }, "notifications": { "sms": true } }
     */
    suspend fun updateMetaData(userId: String, metaData: Map<String, Any?>): Map<String, Any?> {
        val response = superTokens.client.put(PATH_META_DATA) {

            header(Constants.HEADER_RECIPE_ID, SessionRecipe.ID)

            setBody(
                UpdateUserMetaDataRequest(
                    userId = userId,
                    metadataUpdate = metaData.toJsonElement(),
                )
            )
        }

        return response.parse<UserMetaDataResponse, Map<String, Any?>> {
            it.metadata.extractedContent
        }
    }

    /**
     * Removes the entire metadata JSON stored about the user.
     */
    suspend fun deleteMetaData(userId: String): SuperTokensStatus {
        val response = superTokens.client.post(PATH_META_DATA_REMOVE) {

            header(Constants.HEADER_RECIPE_ID, SessionRecipe.ID)

            setBody(
                DeleteUserMetaDataRequest(
                    userId = userId,
                )
            )
        }

        return response.parse<StatusResponse, SuperTokensStatus> {
            it.status.toStatus()
        }
    }

    companion object {
        const val ID = "usermetadata"

        const val PATH_META_DATA = "/recipe/user/metadata"
        const val PATH_META_DATA_REMOVE = "/recipe/user/metadata/remove"
    }

}

val UserMetaData = object: RecipeBuilder<UserMetaDataRecipeConfig, UserMetaDataRecipe>() {

    override fun install(configure: UserMetaDataRecipeConfig.() -> Unit): (SuperTokens) -> UserMetaDataRecipe {
        val config = UserMetaDataRecipeConfig().apply(configure)

        return {
            UserMetaDataRecipe(it, config)
        }
    }

}

/**
 * Gets the stored metadata object of the user
 */
suspend fun SuperTokens.getUserMetaData(
    userId: String,
) = getRecipe<UserMetaDataRecipe>().getMetaData(userId)

/**
 * Updates the metadata object stored about the user by doing a shallow merge of the stored and the update JSONs and removing properties set to null on the root level of the update object. The merged object is then reserialized and stored.
 *  e.g.:
 *  stored: { "preferences": { "theme":"dark" }, "notifications": { "email": true }, "todos": ["example"] }
 *  update: { "notifications": { "sms": true }, "todos": null }
 *  result: { "preferences": { "theme":"dark" }, "notifications": { "sms": true } }
 */
suspend fun SuperTokens.updateUserMetaData(
    userId: String,
    metaData: Map<String, Any?>,
) = getRecipe<UserMetaDataRecipe>().updateMetaData(userId, metaData)

/**
 * Removes the entire metadata JSON stored about the user.
 */
suspend fun SuperTokens.deleteUserMetaData(
    userId: String,
) = getRecipe<UserMetaDataRecipe>().deleteMetaData(userId)