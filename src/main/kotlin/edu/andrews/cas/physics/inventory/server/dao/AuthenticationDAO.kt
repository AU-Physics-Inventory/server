package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.request.UserRegistration
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.exception.RegistrationNotFoundException
import edu.andrews.cas.physics.inventory.server.model.User
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.reactive.UserFinder
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneResponse
import edu.andrews.cas.physics.inventory.server.reactive.UpdateResponse
import edu.andrews.cas.physics.inventory.server.response.RegistrationResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Component
open class AuthenticationDAO @Autowired constructor(private val mongodb: MongoDatabase, private val userDAO: UserDAO) {
    fun registerUser(userRegistration: UserRegistration, salt: String) : RegistrationResponse {
        logger.info("[Auth DAO] Registering user {}", userRegistration.username)
        if (userRegistration.accessCode != null) return registerPreRegisteredUser(userRegistration, salt)
        val usersWithEmail = userDAO.findUserByEmail(userRegistration.email).get()
        val usersWithUsername = userDAO.findUserByName(userRegistration.username).get()
        val emailTaken = usersWithEmail.isNotEmpty()
        val usernameTaken = usersWithUsername.isNotEmpty()
        val response = RegistrationResponse(usernameTaken, emailTaken)
        logger.info("[Auth DAO] Registration of user '{}' successful? {}", userRegistration.username, response.isSuccess)
        if (response.isSuccess) {
            val future = CompletableFuture<Boolean>()
            val insertResponse = InsertOneResponse(future)
            val collection = mongodb.getCollection(AUTH_COLLECTION)
            val userDocument = User()
                .status(UserStatus.ACTIVE)
                .email(userRegistration.email)
                .username(userRegistration.username)
                .password(userRegistration.password)
                .salt(salt)
                .build()
            collection.insertOne(userDocument).subscribe(insertResponse)
            val ack = future.get()
            if (!ack.equals(true)) throw DatabaseException()
        }
        return response
    }

    fun registerPreRegisteredUser(userRegistration: UserRegistration, salt: String): RegistrationResponse {
        logger.info("[Auth DAO] Attempting to register a pre-registered user with access code {} and email {}", userRegistration.accessCode, userRegistration.email)
        val users = userDAO.findUserByEmail(userRegistration.email).get()
        if (users.isEmpty() || !users[0].accessCode.equals(userRegistration.accessCode)) throw RegistrationNotFoundException()
        if (userDAO.findUserByName(userRegistration.username).get().isNotEmpty()) return RegistrationResponse(true, false);
        val user = users[0].username(userRegistration.username).password(userRegistration.password).salt(salt).status(UserStatus.ACTIVE)
        val future = CompletableFuture<Boolean>()
        val response = UpdateResponse(future)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        collection.updateOne(eq("email", userRegistration.email), user.build()).subscribe(response)
        val ack = future.get()
        if (!ack.equals(true)) throw DatabaseException()
        return RegistrationResponse(false, false)
    }

    fun loginAttempt(user: String, valid: Boolean) {
        logger.info("[Auth DAO] Setting login attempt for user: {}\tValid: {}", user, valid)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        val future = CompletableFuture<Boolean>()
        val updateResponse = UpdateResponse(future)
        val now = LocalDateTime.now()
        if (valid) collection.updateOne(
            eq("username", user),
            combine(
                set("failed_attempts", 0),
                set("last_attempt", now),
                set("last_success", now)))
            .subscribe(updateResponse)
        else collection.updateOne(
            eq("username", user),
            combine(
                inc("failed_attempts", 1),
                set("last_attempt", now)))
            .subscribe(updateResponse)
        future.whenCompleteAsync { _, _ -> }
    }

    fun findUser(username: String): User? {
        val future = userDAO.findUserByName(username)
        val users = future.get()
        return if (users.isNotEmpty()) users[0] else null
    }

    companion object {
        private const val AUTH_COLLECTION: String = "users"
        private val logger: Logger = LogManager.getLogger();
    }
}