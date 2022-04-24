package edu.andrews.cas.physics.inventory.server.service

import edu.andrews.cas.physics.inventory.server.dao.UserDAO
import edu.andrews.cas.physics.inventory.server.exception.AlreadyVerifiedException
import edu.andrews.cas.physics.inventory.server.exception.NoSuchUserException
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.request.UserRegistration
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Service
class EmailService @Autowired constructor(
    private val mailSession: Session,
    private val userDAO: UserDAO,
    @Qualifier("configProperties") private val config: Properties,
    @Qualifier("emailProperties") private val emailConfig: Properties
) {
    fun sendEmailVerificationEmail(userRegistration: UserRegistration) {
        logger.info("[Email Service] Sending welcome email to user {}", userRegistration.username)
        val template = FileUtils
            .readFileToString(
                File(ClassLoader.getSystemResource("templates/email/welcome_email.html").toURI()),
                Charset.forName("UTF-8")
            )
        val htmlString = template.replace(
            "{{URL}}",
            String.format("%s/verifyEmail?username=%s", config["server.host"], userRegistration.username),
            false
        )
        val subject = "[Physics Inventory] Verify your e-mail"
        sendMessage(validateEmailAddress(userRegistration.email), subject, htmlString)
    }

    fun resendEmailVerificationEmail(user: String) {
        val documentFuture = userDAO.findUserByName(user)
        val users = documentFuture.get()
        if (users.isEmpty()) throw NoSuchUserException(user)
        if (isUserVerified(users[0])) throw AlreadyVerifiedException()
        sendEmailVerificationEmail(
            UserRegistration(
                users[0].email,
                user,
                null,
                null,
                false
            )
        )
    }

    fun sendPasswordResetEmail(email: String) {
        return
        TODO("Not yet implemented")
    }

    fun sendPreRegisteredUserRegistrationSuccessEmail(emailAddress: String) {
        logger.info("[Email Service] Sending verification success email to address {}", emailAddress)
        val template = FileUtils
            .readFileToString(
                File(
                    ClassLoader.getSystemResource("templates/email/preregistered_user_registration_success_email.html")
                        .toURI()
                ),
                Charset.forName("UTF-8")
            )
        val subject = "Welcome to Physics Inventory"
        sendMessage(validateEmailAddress(emailAddress), subject, template)
    }

    fun sendRegistrationEmail(email: String, accessCode: String) {
        return
        TODO("Not yet implemented")
    }

    fun validateEmailAddress(emailAddress: String): InternetAddress {
        logger.info("[Email Service] Validating email address: {}", emailAddress)
        return InternetAddress.parse(emailAddress)[0]
    }

    fun verifyEmail(user: String) {
        logger.info("[Email Service] Attempting to verify email for {}", user)
        if (isUserVerified(user)) throw AlreadyVerifiedException()
        val userEmailAddress = userDAO.verifyEmail(user)
        sendVerificationSuccessEmail(userEmailAddress)
    }

    private fun sendVerificationSuccessEmail(emailAddress: String) {
        logger.info("[Email Service] Sending verification success email to address {}", emailAddress)
        val template = FileUtils
            .readFileToString(
                File(ClassLoader.getSystemResource("templates/email/email_verification_success_email.html").toURI()),
                Charset.forName("UTF-8")
            )
        val subject = "Welcome to Physics Inventory"
        sendMessage(validateEmailAddress(emailAddress), subject, template)
    }

    private fun isUserVerified(user: String): Boolean {
        logger.info("[Email Service] Checking if user is verified")
        val documentFuture = userDAO.findUserByName(user)
        val users = documentFuture.get()
        if (users.isEmpty()) throw NoSuchUserException(user)
        return users[0].isEmailVerified
    }

    private fun isUserVerified(user: User): Boolean {
        return user.isEmailVerified
    }

    private fun sendMessage(to: InternetAddress, subject: String, content: String, contentType: String = "text/html") {
        val msg = MimeMessage(mailSession)
        msg.setFrom(validateEmailAddress(emailConfig.getProperty("mail.from")))
        msg.setRecipient(Message.RecipientType.TO, to)
        msg.subject = subject
        msg.setContent(content, contentType)
        msg.sentDate = Date.from(Instant.now())
        Transport.send(msg)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}