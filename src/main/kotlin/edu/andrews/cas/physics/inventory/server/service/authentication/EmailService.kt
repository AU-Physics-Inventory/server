package edu.andrews.cas.physics.inventory.server.service.authentication

import edu.andrews.cas.physics.inventory.server.dao.UserDAO
import edu.andrews.cas.physics.inventory.server.exception.AlreadyVerifiedException
import edu.andrews.cas.physics.inventory.server.exception.NoSuchUserException
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.request.user.UserRegistration
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.AddressException
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.time.Instant
import java.util.*

@Service
class EmailService @Autowired constructor(
    private val mailSession: Session,
    private val userDAO: UserDAO,
    @Qualifier("configProperties") private val config: Properties,
    @Qualifier("emailProperties") private val emailConfig: Properties
) {
    fun sendEmailVerificationEmail(userRegistration: UserRegistration) {
        logger.info("[Email Service] Sending welcome email to user {}", userRegistration.username)
        val template = IOUtils.toString(
            ClassPathResource("templates/email/verify_email.html").inputStream,
                Charset.forName("UTF-8")
            )
        val htmlString = template.replace(
            "{{URL}}",
            String.format("%s/verifyEmail?username=%s", config["server.host"], userRegistration.username),
            false
        )
        val subject = "[Physics Inventory] Verify your e-mail"
        sendMessage(parseEmailAddress(userRegistration.email), subject, htmlString)
    }

    fun resendEmailVerificationEmail(user: String) {
        val documentFuture = userDAO.findUserByName(user)
        val users = documentFuture.get()
        if (users.isEmpty()) throw NoSuchUserException(user)
        if (isUserVerified(users[0])) throw AlreadyVerifiedException()
        sendEmailVerificationEmail(UserRegistration(null, null, users[0].email, user, null, null, false))
    }

    fun sendPasswordResetEmail(email: String) {
        return
        TODO("Not yet implemented")
    }

    fun sendPreRegisteredUserRegistrationSuccessEmail(emailAddress: String) {
        logger.info("[Email Service] Sending verification success email to address {}", emailAddress)
        val template = IOUtils.toString(
            ClassPathResource("templates/email/preregistered_user_registration_success_email.html").inputStream,
            Charset.forName("UTF-8")
        )
        val subject = "Welcome to Physics Inventory"
        sendMessage(parseEmailAddress(emailAddress), subject, template)
    }

    fun sendRegistrationEmail(email: String, accessCode: String) {
        logger.info(
            "[Email Service] Sending registration invitation to email address '{}' with access code '{}'",
            email,
            accessCode
        )
        val template = IOUtils.toString(
            ClassPathResource("templates/email/registration_invitation.html").inputStream,
            Charset.forName("UTF-8")
        )
        val htmlString = template.replace(
                "{{URL}}",
                String.format("%s/register?email=%s&accessCode=%s", config["webapp.host"], email, accessCode),
                false
            ).replace(
                "{{CODE}}", accessCode, false
            )
        val subject = "[Physics Inventory] Invitation to register"
        sendMessage(parseEmailAddress(email), subject, htmlString)
    }

    private fun validateEmailAddress(emailAddress: String): Boolean {
        logger.info("[Email Service] Validating email address: {}", emailAddress)
        val regex = "^[a-zA-Z0-9_!#\$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$".toRegex()
        return emailAddress.matches(regex)
    }

    fun parseEmailAddress(emailAddress: String): InternetAddress {
        logger.info("[Email Service] Parsing email address: {}", emailAddress)
        if (validateEmailAddress(emailAddress)) return InternetAddress.parse(emailAddress)[0]
        else throw AddressException()
    }

    fun verifyEmail(user: String) {
        logger.info("[Email Service] Attempting to verify email for {}", user)
        if (isUserVerified(user)) throw AlreadyVerifiedException()
        val userEmailAddress = userDAO.verifyEmail(user)
        sendVerificationSuccessEmail(userEmailAddress)
    }

    private fun sendVerificationSuccessEmail(emailAddress: String) {
        logger.info("[Email Service] Sending verification success email to address {}", emailAddress)
        val template = IOUtils.toString(
            ClassPathResource("templates/email/email_verification_success_email.html").inputStream,
            Charset.forName("UTF-8")
        )
        val subject = "Welcome to Physics Inventory"
        sendMessage(parseEmailAddress(emailAddress), subject, template)
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
        msg.setFrom(parseEmailAddress(emailConfig.getProperty("mail.from")))
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