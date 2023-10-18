package edu.andrews.cas.physics.inventory.server.controller.authentication

import edu.andrews.cas.physics.inventory.server.exception.AlreadyVerifiedException
import edu.andrews.cas.physics.inventory.server.service.authentication.EmailService
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.Charset

@RestController
class EmailController @Autowired constructor(private val emailService: EmailService) {

    //TODO: Implement better email verification API
    //it should not be that easy that you can send a GET request with the username as a param
    @GetMapping("/verifyEmail")
    fun verifyEmail(@RequestParam username: String) : String {
        logger.info("[Email Controller] Received e-mail verification request from user {}", username)
        return try {
            emailService.verifyEmail(username)
            FileUtils
                .readFileToString(
                    ClassPathResource("templates/web/email_verification_successful.html").file,
                    Charset.forName("UTF-8"))
        } catch (e: AlreadyVerifiedException) {
            FileUtils
                .readFileToString(
                    ClassPathResource("templates/web/email_already_verified.html").file,
                    Charset.forName("UTF-8"))
        }
    }

    @PostMapping("/resendWelcomeEmail")
    fun resendWelcomeEmail(@RequestBody username: String) : ResponseEntity<Any> {
        logger.info("[Email Controller] Received request to resend welcome email for {}", username)
        emailService.resendEmailVerificationEmail(username)
        return ResponseEntity.accepted().build()
    }

    companion object {
        private var logger: Logger = LogManager.getLogger()
    }
}