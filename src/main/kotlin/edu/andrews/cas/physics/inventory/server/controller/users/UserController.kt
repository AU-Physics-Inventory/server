package edu.andrews.cas.physics.inventory.server.controller.users

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.service.users.UserService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/user")
class UserController @Autowired constructor(private val userService: UserService) {
    @PostMapping("/changeEmail")
    fun changeEmail(@RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken, @RequestBody email: String) : ResponseEntity<Any> {
        logger.info("[User Controller] Received request to change e-mail")
        userService.changeEmail(jwt, email.lowercase(Locale.getDefault()))
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/changePassword")
    fun changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) jwt: AuthorizationToken, @RequestBody password: String) : ResponseEntity<Any> {
        logger.info("[User Controller] Received request to update password")
        try {
            userService.changePassword(jwt, password)
        } catch (e: IllegalArgumentException) {
            logger.error(e)
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}