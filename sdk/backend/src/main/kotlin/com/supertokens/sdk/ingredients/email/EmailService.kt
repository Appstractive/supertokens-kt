package com.supertokens.sdk.ingredients.email

data class EmailContent(
    val body: String,
    val isHtml: Boolean,
    val subject: String,
    val toEmail: String,
)

interface EmailService {

    suspend fun sendEmail(content: EmailContent)

}