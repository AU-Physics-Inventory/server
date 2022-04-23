package edu.andrews.cas.physics.inventory.server.controller

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.request.UserLogin
import edu.andrews.cas.physics.inventory.server.service.AuthenticationService
import edu.andrews.cas.physics.inventory.server.request.UserRegistration
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.response.ErrorResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
class AuthenticationController @Autowired constructor(private val authenticationService: AuthenticationService) {

    @PostMapping("/login")
    fun  login(@RequestBody userLogin: UserLogin) : ResponseEntity<AuthorizationToken> {
        logger.info("[Auth Controller] Login request received for user: {}", userLogin.username)
        val token = this.authenticationService.authenticateUser(userLogin)
        return if (token == null) {
            logger.info("[Auth Controller] Unable to log in user {}", userLogin.username)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } else {
            logger.info("[Auth Controller] User logged in successfully - returning JWT to user {}", userLogin.username)
            ResponseEntity.status(HttpStatus.OK).body(token)
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody registration: UserRegistration) : ResponseEntity<Any> {
        return try {
            val response = this.authenticationService.registerUser(registration)
            ResponseEntity.status(if (response.isSuccess) HttpStatus.OK else HttpStatus.BAD_REQUEST).body(response)
        } catch (e: DatabaseException) {
            logger.error(e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Unable to register user", "An unexpected database error occurred"))
        }
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken) : ResponseEntity<Any> {
        logger.info("[Auth Controller] Logging out user with jwt {}", jwt)
        return if (authenticationService.logout(jwt)) ResponseEntity.ok().build()
        else ResponseEntity.internalServerError().build()
    }

    @GetMapping("/resetPassword")
    fun resetPassword(@RequestParam("id") userIdentification: String, @RequestParam("type") identificationType: String) : ResponseEntity<Any> {
        logger.info("[Auth Controller] Received password reset GET request", identificationType, userIdentification)
        if (identificationType != "email" && identificationType != "username") return ResponseEntity.badRequest().body("Type must be either 'email' or 'username'")
        logger.info("[Auth Controller] Requesting a password reset for user with {}: {}", identificationType, userIdentification)
        authenticationService.resetPassword(userIdentification, identificationType)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/resetPassword")
    fun resetPassword(userLogin: UserLogin) : ResponseEntity<Any> {
        logger.info("[Auth Controller] Received password reset POST request for user: {}", userLogin.username)
        authenticationService.setPassword(userLogin.username, userLogin.password)
        return ResponseEntity.accepted().build()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}