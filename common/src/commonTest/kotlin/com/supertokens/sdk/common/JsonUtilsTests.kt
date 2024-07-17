package com.supertokens.sdk.common

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

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

val jsonEncoder = Json { encodeDefaults = true }

class MapUtilsTests {

  @Test
  fun testSerializeMap() {
    val map: Map<String, Any?> =
        mapOf(
            "null" to null,
            "bool" to true,
            "number" to 123.456,
            "string" to "Hello",
            "enum" to TestEnum.VALUE1,
            "list" to
                listOf<Any?>(
                    null,
                    false,
                    789.123,
                    "World",
                    TestEnum.VALUE2,
                ),
            "object" to
                mapOf<String, Any?>(
                    "null" to null,
                    "bool" to true,
                    "number" to 42,
                    "string" to "Hello",
                    "enum" to TestEnum.VALUE3,
                ),
            "complex" to
                jsonEncoder.encodeToJsonElement(
                    TestClass(
                        bool = true,
                        number = 169.270,
                        string = "!",
                        enum = TestEnum.VALUE2,
                    )),
        )

    val encoded = map.toJsonElement()

    assertTrue(encoded is JsonObject)
  }
}
