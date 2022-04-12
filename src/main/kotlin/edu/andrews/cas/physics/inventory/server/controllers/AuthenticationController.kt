package edu.andrews.cas.physics.inventory.server.controllers

import edu.andrews.cas.physics.inventory.server.auth.AuthenticationToken
import edu.andrews.cas.physics.inventory.server.request.UserLogin
import edu.andrews.cas.physics.inventory.server.service.AuthenticationService
import edu.andrews.cas.physics.inventory.server.request.UserRegistration
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.response.ErrorResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class AuthenticationController @Autowired constructor(private val authenticationService: AuthenticationService) {

    @PostMapping("/login")
    fun  login(@RequestBody userLogin: UserLogin) : ResponseEntity<AuthenticationToken> {
        logger.info("[Auth Controller] Login request received for user: {}", userLogin.username)
        val token = this.authenticationService.authenticateUser(userLogin);
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

    // TODO IMPLEMENT LOGOUT

    companion object {
        private val logger: Logger = LogManager.getLogger();
    }
}