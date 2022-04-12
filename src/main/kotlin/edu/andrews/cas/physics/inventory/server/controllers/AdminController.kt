package edu.andrews.cas.physics.inventory.server.controllers

import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.request.UserInvitation
import edu.andrews.cas.physics.inventory.server.service.AdminService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.net.http.HttpResponse

@Controller
class AdminController @Autowired constructor(private val service: AdminService) {

    @PostMapping("/admin/inviteUser")
    fun inviteUserToRegister(@RequestBody userInvitation: UserInvitation) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to invite user with email address {} to register", userInvitation.email)
        return try {
            val registrationCode = service.inviteUserToRegister(userInvitation)
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"registrationCode\": \"$registrationCode\"}")
        } catch (e: AlreadyRegisteredException) {
            logger.info("[Admin Controller] User is already registered. Returning error response.")
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"User is already registered.\"}")
        } catch (e: Exception) {
            logger.error(e)
            ResponseEntity.internalServerError().build()
        }
    }

    // TODO IMPLEMENT LOGOUT USER

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}