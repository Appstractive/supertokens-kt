package com.supertokens.sdk

import com.supertokens.sdk.recipes.passwordless.PasswordlessRecipe
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path

fun SuperTokens.buildRequestPath(
    path: String,
    tenantId: String?,
): String {
    return listOfNotNull<String>(
        appId?.let { "/appid-$it" },
        tenantId?.let { "/$it" },
        path,
    ).joinToString("")
}

suspend inline fun SuperTokens.get(
    urlString: String,
    tenantId: String?,
    queryParams: Map<String, String> = emptyMap(),
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.get {
    url()
    url {
        path(buildRequestPath(
            path = urlString,
            tenantId = tenantId,
        ))
        queryParams.forEach {
            parameters.append(it.key, it.value)
        }
    }
    block()
}

suspend inline fun SuperTokens.post(
    urlString: String,
    tenantId: String?,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.post {
    url(buildRequestPath(
        path = urlString,
        tenantId = tenantId,
    ))
    block()
}

suspend inline fun SuperTokens.put(
    urlString: String,
    tenantId: String?,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.put {
    url(buildRequestPath(
        path = urlString,
        tenantId = tenantId,
    ))
    block()
}

suspend inline fun SuperTokens.delete(
    urlString: String,
    tenantId: String?,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.delete {
    url(buildRequestPath(
        path = urlString,
        tenantId = tenantId,
    ))
    block()
}
