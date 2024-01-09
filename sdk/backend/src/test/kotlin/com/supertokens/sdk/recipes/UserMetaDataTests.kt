package com.supertokens.sdk.recipes

import com.supertokens.sdk.AppConfig
import com.supertokens.sdk.common.SuperTokensStatus
import com.supertokens.sdk.core.getUsersByEMail
import com.supertokens.sdk.recipe
import com.supertokens.sdk.recipes.usermetadata.UserMetaData
import com.supertokens.sdk.recipes.usermetadata.UserMetaDataRecipe
import com.supertokens.sdk.recipes.usermetadata.deleteUserMetaData
import com.supertokens.sdk.recipes.usermetadata.getUserMetaData
import com.supertokens.sdk.recipes.usermetadata.updateUserMetaData
import com.supertokens.sdk.superTokens
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserMetaDataTests {

    private enum class TestEnum {
        VALUE1,
        VALUE2,
        VALUE3,
    }

    @Serializable
    private data class TestClass(
        val bool: Boolean,
        val number: Double,
        val string: String,
        val enum: TestEnum,
    )

    private val superTokens = superTokens(
        connectionURI = "https://try.supertokens.com/",
        appConfig = AppConfig(
            name = "TestApp",
        ),
    ) {
        recipe(UserMetaData)
    }

    @Test
    fun testConfig() {
        val recipe = superTokens.getRecipe<UserMetaDataRecipe>()
    }

    @Test
    fun testUpdateMetaData() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()

        val response = superTokens.updateUserMetaData(
            user.id,
            metaData,
        )

        val metaDataResponse = superTokens.getUserMetaData(user.id)
        assertEquals(response, metaDataResponse)
    }

    @Test
    fun testDeleteMetaData() = runBlocking {
        val user = superTokens.getUsersByEMail(TEST_USER).first()

        superTokens.updateUserMetaData(
            user.id,
            metaData,
        )

        val response = superTokens.deleteUserMetaData(user.id)
        assertEquals(SuperTokensStatus.OK, response)

        val metaDataResponse = superTokens.getUserMetaData(user.id)
        assertTrue(metaDataResponse.isEmpty())
    }

    companion object {
        const val TEST_USER = "test@test.de"

        private val jsonEncoder = Json { encodeDefaults = true }

        val metaData: Map<String, Any?> = mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to listOf<Any?>(
                null,
                false,
                789.123,
                "World",
                TestEnum.VALUE2,
            ),
            "object" to mapOf<String, Any?>(
                "null" to null,
                "bool" to true,
                "number" to 42,
                "string" to "Hello",
                "enum" to TestEnum.VALUE3,
            ),
            "complex" to jsonEncoder.encodeToJsonElement(
                TestClass(
                    bool = true,
                    number = 169.270,
                    string = "!",
                    enum = TestEnum.VALUE2,
                )
            ),
        )
    }

}