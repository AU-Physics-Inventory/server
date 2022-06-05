package edu.andrews.cas.physics.inventory.server.controller.users

import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.model.User
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.request.user.ChangeUserStatusRequest
import edu.andrews.cas.physics.inventory.server.request.user.UserInvitation
import edu.andrews.cas.physics.inventory.server.request.user.UserRole
import edu.andrews.cas.physics.inventory.server.service.authentication.AuthenticationService
import edu.andrews.cas.physics.inventory.server.service.users.AdminService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
class AdminController @Autowired constructor(private val adminService: AdminService, private val authService: AuthenticationService) {

    @PostMapping("/user/invite")
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

    @PostMapping("/user/addRole")
    fun addUserRole(@RequestBody userRole: UserRole) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to add role '{}' to user '{}'", userRole.role, userRole.username)
        adminService.addUserRole(userRole)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/user/removeRole")
    fun removeUserRole(@RequestBody userRole: UserRole) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to remove role '{}' from user '{}'", userRole.role, userRole.username)
        adminService.removeUserRole(userRole)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/user/logout")
    fun logoutUser(@RequestBody user: String) : ResponseEntity<Any> {
        return if (authService.logout(user)) ResponseEntity.ok().build()
        else ResponseEntity.internalServerError().build()
    }

    @PostMapping("/user/changeStatus")
    fun changeUserStatus(@RequestBody request: ChangeUserStatusRequest) : ResponseEntity<Any> {
        logger.info("[Admin Controller] Received request to set status for user '{}' as '{}'", request.username, request.status)
        if (request.status == UserStatus.LOCKED || request.status == UserStatus.PENDING) return ResponseEntity.badRequest().body("Administrators are only able to activate or deactivate users.")
        adminService.changeUserStatus(request)
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/users")
    fun getUsers(@RequestParam("withRole", required = false) role: String?) : ResponseEntity<List<User>> {
        logger.info("[Admin Controller] Received request to retrieve all user documents {}", if (role != null && role.isNotEmpty()) String.format("with role '%s'", role) else "")
        return ResponseEntity.ok(adminService.getUsers(role))
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}