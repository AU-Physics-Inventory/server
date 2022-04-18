package edu.andrews.cas.physics.inventory.server.controllers

import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.model.User
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.request.UserInvitation
import edu.andrews.cas.physics.inventory.server.request.UserRole
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
    fun addUserRole(@RequestBody userRole: UserRole) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to add role '{}' to user '{}'", userRole.role, userRole.username)
        adminService.addUserRole(userRole)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/admin/removeUserRole")
    fun removeUserRole(@RequestBody userRole: UserRole) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to remove role '{}' from user '{}'", userRole.role, userRole.username)
        adminService.removeUserRole(userRole)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/admin/logoutUser")
    fun logoutUser(@RequestParam user: String) : ResponseEntity<Any> {
        return if (authService.logout(user)) ResponseEntity.ok().build()
        else ResponseEntity.internalServerError().build()
    }

    @GetMapping("/admin/changeUserStatus")
    fun changeUserStatus(@RequestParam user: String, @RequestParam status: UserStatus) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to set status for user '{}' as '{}'", user, status)
        if (status == UserStatus.LOCKED || status == UserStatus.PENDING) return ResponseEntity.badRequest().body("Administrators are only able to activate or deactivate users.")
        adminService.changeUserStatus(user, status)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/admin/users")
    fun getUsers() : ResponseEntity<List<User>> {
        logger.info("[Admin Controller] Received request to retrieve all user documents")
        return ResponseEntity.ok(adminService.getUsers())
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}