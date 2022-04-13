package edu.andrews.cas.physics.inventory.server.controllers

import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.request.UserInvitation
import edu.andrews.cas.physics.inventory.server.request.UserRoles
import edu.andrews.cas.physics.inventory.server.service.AdminService
import edu.andrews.cas.physics.inventory.server.service.AuthenticationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AdminController @Autowired constructor(private val adminService: AdminService, private val authService: AuthenticationService) {

    @PostMapping("/admin/inviteUser")
    fun inviteUserToRegister(@RequestBody userInvitation: UserInvitation) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to invite user with email address {} to register", userInvitation.email)
        return try {
            val registrationCode = adminService.inviteUserToRegister(userInvitation)
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

    @PostMapping("/admin/addUserRole")
    fun addUserRoles(@RequestBody userRoles: UserRoles) {
        logger.info("[Admin Controller] Received request to add roles to user {}", userRoles.username)
    }

    @GetMapping("/admin/logoutUser")
    fun logoutUser(@RequestParam user: String) : ResponseEntity<Any> {
        return if (authService.logout(user)) ResponseEntity.ok().build()
        else ResponseEntity.internalServerError().build()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}