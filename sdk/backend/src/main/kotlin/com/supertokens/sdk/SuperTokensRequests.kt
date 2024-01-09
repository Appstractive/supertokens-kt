package com.supertokens.sdk

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse

fun SuperTokens.buildRequestPath(
    path: String,
    includeAppId: Boolean = true,
    includeTenantId: Boolean = true,
): String {
    return listOfNotNull<String>(
        if(includeAppId) appId?.let { "/appid-$it" } else null,
        if(includeTenantId) tenantId?.let { "/$it" } else null,
        path,
    ).joinToString("")
}

suspend inline fun SuperTokens.get(
    urlString: String,
    includeAppId: Boolean = true,
    includeTenantId: Boolean = true,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.get {
    url(buildRequestPath(
        path = urlString,
        includeAppId = includeAppId,
        includeTenantId = includeTenantId,
    ))
    block()
}

suspend inline fun SuperTokens.post(
    urlString: String,
    includeAppId: Boolean = true,
    includeTenantId: Boolean = true,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.post {
    url(buildRequestPath(
        path = urlString,
        includeAppId = includeAppId,
        includeTenantId = includeTenantId,
    ))
    block()
}

suspend inline fun SuperTokens.put(
    urlString: String,
    includeAppId: Boolean = true,
    includeTenantId: Boolean = true,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.put {
    url(buildRequestPath(
        path = urlString,
        includeAppId = includeAppId,
        includeTenantId = includeTenantId,
    ))
    block()
}

suspend inline fun SuperTokens.delete(
    urlString: String,
    includeAppId: Boolean = true,
    includeTenantId: Boolean = true,
    block: HttpRequestBuilder.() -> Unit = {}
): HttpResponse = client.delete {
    url(buildRequestPath(
        path = urlString,
        includeAppId = includeAppId,
        includeTenantId = includeTenantId,
    ))
    block()
}
