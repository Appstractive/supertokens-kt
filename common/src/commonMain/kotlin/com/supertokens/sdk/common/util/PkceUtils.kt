package com.supertokens.sdk.common.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha2.SHA256

fun generateCodeVerifier(): String {
  val secureRandom = SecureRandom()
  val code = ByteArray(64)
  secureRandom.nextBytesCopyTo(code)
  return code.encodeForPKCE()
}

fun generateCodeChallenge(verifier: String): String {
  val bytes = verifier.encodeToByteArray()
  SHA256().update(bytes, 0, bytes.size)
  return SHA256().digest(bytes).encodeForPKCE()
}

@OptIn(ExperimentalEncodingApi::class)
private fun ByteArray.encodeForPKCE() =
    Base64.encode(this).replace("=", "").replace("+", "-").replace("/", "_")
