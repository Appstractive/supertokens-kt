package com.supertokens.sdk.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

fun List<*>.toJsonElement(): JsonElement {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach { value ->
        when (value) {
            null -> list.add(JsonNull)
            is Map<*, *> -> list.add(value.toJsonElement())
            is List<*> -> list.add(value.toJsonElement())
            is Set<*> -> list.add(value.toList().toJsonElement())
            is Boolean -> list.add(JsonPrimitive(value))
            is Number -> list.add(JsonPrimitive(value))
            is String -> list.add(JsonPrimitive(value))
            is Enum<*> -> list.add(JsonPrimitive(value.toString()))
            is JsonElement -> list.add(value)
            else -> throw IllegalStateException("Can't serialize unknown collection type: $value")
        }
    }
    return JsonArray(list)
}

fun Map<*, *>.toJsonElement(): JsonObject {
    val map = this.mapValues { (_, value) ->
        when (value) {
            null -> JsonNull
            is Map<*, *> -> value.toJsonElement()
            is List<*> -> value.toJsonElement()
            is Set<*> -> value.toList().toJsonElement()
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is Enum<*> -> JsonPrimitive(value.toString())
            is JsonElement -> value
            else -> throw IllegalStateException("Can't serialize unknown type: $value")
        }
    }.mapKeys {
        it.key.toString()
    }
    return JsonObject(map)
}

val JsonElement.extractedContent: Any?
    get() {
        if (this is JsonPrimitive) {
            if (this.isString) {
                return this.content
            }
            return this.jsonPrimitive.booleanOrNull ?: this.jsonPrimitive.intOrNull ?: this.jsonPrimitive.longOrNull ?: this.jsonPrimitive.floatOrNull
            ?: this.jsonPrimitive.doubleOrNull ?: this.jsonPrimitive.contentOrNull
        }
        if (this is JsonArray) {
            return this.jsonArray.map {
                it.extractedContent
            }
        }
        if (this is JsonObject) {
            return this.jsonObject.extractedContent
        }
        return null
    }

val JsonObject.extractedContent: Map<String, Any?>
    get() = entries.associate {
        it.key to it.value.extractedContent
    }