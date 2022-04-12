package edu.andrews.cas.physics.inventory.server.service

import edu.andrews.cas.physics.inventory.server.dao.AdminDAO
import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.model.User
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.request.UserInvitation
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
        try {
            adminDAO.register(user)
            emailService.sendRegistrationEmail(userInvitation.email, accessCode)
        } catch (e: DatabaseException) {
            throw e
        } catch (e: AlreadyRegisteredException) {
            throw e
        }
        return accessCode
    }

    private fun generateAccessCode(): String {
        return RandomStringUtils.randomAlphanumeric(6)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}