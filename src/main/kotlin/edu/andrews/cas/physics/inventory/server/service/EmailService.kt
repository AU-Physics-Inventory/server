package edu.andrews.cas.physics.inventory.server.service

import edu.andrews.cas.physics.inventory.server.auth.UserRegistration
import edu.andrews.cas.physics.inventory.server.dao.UserDAO
import edu.andrews.cas.physics.inventory.server.exception.AlreadyVerifiedException
import edu.andrews.cas.physics.inventory.server.exception.NoSuchUserException
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.MessageFormat
import java.time.Instant
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Service
class EmailService @Autowired constructor(private val mailSession: Session,
                                          private val userDAO: UserDAO,
          @Qualifier("configProperties") private val config: Properties,
          @Qualifier("emailProperties") private val emailConfig: Properties){
    fun sendWelcomeEmail(userRegistration: UserRegistration) {
        logger.info("[Email Service] Sending welcome email to user {}", userRegistration.username)
        val msg = MimeMessage(mailSession)
        val template = FileUtils
            .readFileToString(
                File(ClassLoader.getSystemResource("templates/welcome_email.html").toURI()),
                Charset.forName("UTF-8"))
        val htmlString = template.replace(
            "{{URL}}",
            String.format("%s/verifyEmail?username=%s", config["server.host"], userRegistration.username),
            false)
        logger.debug(htmlString)
        msg.setFrom(validateEmailAddress(emailConfig.getProperty("mail.from")))
        msg.setRecipient(Message.RecipientType.TO, validateEmailAddress(userRegistration.email))
        msg.subject = "[Physics Inventory] Verify your e-mail"
        msg.setContent(htmlString, "text/html")
        msg.sentDate = Date.from(Instant.now())
        Transport.send(msg)
    }

    fun validateEmailAddress(emailAddress: String) : InternetAddress {
        logger.info("[Email Service] Validating email address: {}", emailAddress)
        return InternetAddress.parse(emailAddress)[0]
    }

    fun verifyEmail(user: String) {
        logger.info("[Email Service] Setting email as verified for ", user)
        val userEmailAddress = userDAO.verifyEmail(user)
        sendVerificationSuccessEmail(userEmailAddress)
    }

    private fun sendVerificationSuccessEmail(emailAddress: String) {
        logger.info("[Email Service] Sending verification success email to address {}", emailAddress)
        val msg = MimeMessage(mailSession)
        val template = FileUtils
            .readFileToString(
                File(ClassLoader.getSystemResource("templates/verification_success_email.html").toURI()),
                Charset.forName("UTF-8"))
        msg.setFrom(validateEmailAddress(emailConfig.getProperty("mail.from")))
        msg.setRecipient(Message.RecipientType.TO, validateEmailAddress(emailAddress))
        msg.subject = "Welcome to Physics Inventory"
        msg.setContent(template, "text/html")
        msg.sentDate = Date.from(Instant.now())
        Transport.send(msg)
    }

    fun resendWelcomeEmail(user: String) {
        val documentFuture = userDAO.findUser(user)
        val userDocuments = documentFuture.get()
        if (userDocuments.isEmpty()) throw NoSuchUserException(user)
        if (userDocuments[0].getBoolean("email_verified")) throw AlreadyVerifiedException()
        sendWelcomeEmail(UserRegistration(userDocuments[0].getString("email"), user, null))
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}