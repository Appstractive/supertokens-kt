package com.supertokens.sdk.common.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.serializer

object StringListSerializer : JsonTransformingSerializer<List<String>>(serializer<List<String>>()) {
  // If response is not an array, then it is a single object that should be wrapped into the array
  override fun transformDeserialize(element: JsonElement): JsonElement =
      if (element !is JsonArray) JsonArray(listOf(element)) else element
}
