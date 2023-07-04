package com.supertokens.sdk.ingredients.email.smtp

import com.supertokens.sdk.ingredients.email.EmailContent
import com.supertokens.sdk.ingredients.email.EmailService
import org.apache.commons.mail.HtmlEmail
import java.util.Properties
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String? = null,
    val password: String,
    val fromEmail: String,
    val fromName: String,
) {
    val authUsername = username ?: fromEmail
}

class SmtpEmailService(
    private val config: SmtpConfig,
): EmailService() {

    private val transport: Transport by lazy {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        val mailSession = Session.getDefaultInstance(props, null)
        mailSession.getTransport("smtp")
    }

    override suspend fun sendEmail(content: EmailContent) {
        if(!transport.isConnected) {
            transport.connect(config.host, config.port, config.authUsername, config.password)
        }

        val m = HtmlEmail().apply {
            hostName = config.host
            subject = content.subject
            if(content.isHtml) {
                setHtmlMsg(content.body)
            }
            else {
                setTextMsg(content.body)
            }
            setFrom(config.fromEmail, config.fromName)
            addTo(content.toEmail)
            setReplyTo(listOf(InternetAddress(config.fromEmail)))
            buildMimeMessage()
        }.mimeMessage

        transport.sendMessage(m, m.allRecipients)
    }
}