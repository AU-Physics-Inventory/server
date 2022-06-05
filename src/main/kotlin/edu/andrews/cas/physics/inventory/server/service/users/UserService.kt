package edu.andrews.cas.physics.inventory.server.service.users

import edu.andrews.cas.physics.inventory.server.auth.AuthorizationToken
import edu.andrews.cas.physics.inventory.server.dao.UserDAO
import edu.andrews.cas.physics.inventory.server.request.user.UserRegistration
import edu.andrews.cas.physics.inventory.server.service.authentication.AuthenticationService
import edu.andrews.cas.physics.inventory.server.service.authentication.EmailService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.crypto.SecretKey

@Service
class UserService @Autowired constructor(
    private val secretKey: SecretKey,
    private val userDAO: UserDAO,
    private val emailService: EmailService,
    private val authenticationService: AuthenticationService
) {
    fun changeEmail(jwt: AuthorizationToken, email: String) {
        val claims = jwt.getClaims(secretKey)
        val user = claims.body.subject
        logger.info("[User Service] Handling request to update email for user '{}' to '{}'", user, email)
        emailService.validateEmailAddress(email)
        userDAO.updateEmail(user, email)
        emailService.sendEmailVerificationEmail(
            UserRegistration(
                email,
                user,
                null,
                null,
                false
            )
        )
    }

    fun changePassword(jwt: AuthorizationToken, password: String) {
        val claims = jwt.getClaims(secretKey)
        val user = claims.body.subject
        logger.info("[User Service] Handling request to change password for user '{}'", user)
        authenticationService.setPassword(user, password)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
    }
}
