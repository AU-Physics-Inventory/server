package edu.andrews.cas.physics.inventory.server.controllers

import edu.andrews.cas.physics.inventory.server.exception.AlreadyVerifiedException
import edu.andrews.cas.physics.inventory.server.service.EmailService
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File
import java.nio.charset.Charset

@Controller
class EmailController @Autowired constructor(private val emailService: EmailService) {
    @GetMapping("/verifyEmail")
    @ResponseBody
    fun verifyEmail(@RequestParam username: String) : String {
        logger.info("[Email Controller] Received e-mail verification request from user {}", username)
        return try {
            emailService.verifyEmail(username)
            FileUtils
                .readFileToString(
                    File(ClassLoader.getSystemResource("templates/email_verification_successful.html").toURI()),
                    Charset.forName("UTF-8"))
        } catch (e: AlreadyVerifiedException) {
            FileUtils
                .readFileToString(
                    File(ClassLoader.getSystemResource("templates/email_already_verified.html").toURI()),
                    Charset.forName("UTF-8"))
        }
    }

    @GetMapping("/resendWelcomeEmail")
    fun resendWelcomeEmail(@RequestParam username: String) : ResponseEntity<Any> {
        logger.info("[Email Controller] Received request to resend welcome email for {}", username)
        emailService.resendWelcomeEmail(username)
        return ResponseEntity.accepted().build()
    }

    companion object {
        private var logger: Logger = LogManager.getLogger()
    }
}