package edu.andrews.cas.physics.inventory.server.service.users

import edu.andrews.cas.physics.inventory.server.dao.AdminDAO
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.request.user.ChangeUserStatusRequest
import edu.andrews.cas.physics.inventory.server.request.user.UserInvitation
import edu.andrews.cas.physics.inventory.server.request.user.UserRole
import edu.andrews.cas.physics.inventory.server.service.authentication.EmailService
import org.apache.commons.lang3.RandomStringUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminService @Autowired constructor(private val adminDAO: AdminDAO, private val emailService: EmailService) {
    fun inviteUserToRegister(userInvitation: UserInvitation) : String {
        logger.info("[Admin Service] Inviting user with email '{}' to register", userInvitation.email)
        val accessCode = generateAccessCode()
        val user = User()
            .status(UserStatus.PENDING)
            .email(userInvitation.email)
            .roles(userInvitation.roles)
            .accessCode(accessCode)
            .username(null)
            .password(null)
            .salt(null)
            .build()
        adminDAO.register(user)
        emailService.sendRegistrationEmail(userInvitation.email, accessCode)
        return accessCode
    }

    private fun generateAccessCode(): String {
        return RandomStringUtils.randomAlphanumeric(6).uppercase(Locale.getDefault())
    }

    fun addUserRole(userRole: UserRole) {
        logger.info("[Admin Service] Handling request to add role '{}' to user '{}'", userRole.role, userRole.username)
        adminDAO.addUserRole(userRole)
    }

    fun removeUserRole(userRole: UserRole) {
        logger.info("[Admin Service] Handling request to remove role '{}' from user '{}'", userRole.role, userRole.username)
        adminDAO.removeUserRole(userRole)
    }

    fun changeUserStatus(changeUserStatusRequest: ChangeUserStatusRequest) {
        logger.info("[Admin Service] Handling request to set status for user '{}' as '{}'", changeUserStatusRequest.username, changeUserStatusRequest.status)
        adminDAO.setUserStatus(changeUserStatusRequest.username, changeUserStatusRequest.status)
    }

    fun getUsers(role: String?): List<edu.andrews.cas.physics.inventory.server.model.User> {
        return adminDAO.getUsers(role).map { u -> edu.andrews.cas.physics.inventory.server.model.User(u) }.toList()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}