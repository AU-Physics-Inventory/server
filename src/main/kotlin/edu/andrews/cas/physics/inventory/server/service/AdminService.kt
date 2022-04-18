package edu.andrews.cas.physics.inventory.server.service

import edu.andrews.cas.physics.inventory.server.dao.AdminDAO
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.request.UserInvitation
import edu.andrews.cas.physics.inventory.server.request.UserRole
import org.apache.commons.lang3.RandomStringUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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
        return RandomStringUtils.randomAlphanumeric(6)
    }

    fun addUserRole(userRole: UserRole) {
        logger.info("[Admin Service] Handling request to add role '{}' to user '{}'", userRole.role, userRole.username)
        adminDAO.addUserRole(userRole)
    }

    fun removeUserRole(userRole: UserRole) {
        logger.info("[Admin Service] Handling request to remove role '{}' from user '{}'", userRole.role, userRole.username)
        adminDAO.removeUserRole(userRole)
    }

    fun changeUserStatus(user: String, status: UserStatus) {
        logger.info("[Admin Service] Handling request to set status for user '{}' as '{}'", user, status)
        adminDAO.setUserStatus(user, status)
    }

    fun getUsers(): List<edu.andrews.cas.physics.inventory.server.model.User> {
        return adminDAO.getUsers().map { u -> edu.andrews.cas.physics.inventory.server.model.User(u) }.toList()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}