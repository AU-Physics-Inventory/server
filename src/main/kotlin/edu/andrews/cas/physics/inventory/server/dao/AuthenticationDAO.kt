package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.exception.RegistrationNotFoundException
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.reactive.FindOneAndUpdateResponse
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneBooleanResponse
import edu.andrews.cas.physics.inventory.server.reactive.UpdateBooleanResponse
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.request.user.UserRegistration
import edu.andrews.cas.physics.inventory.server.response.RegistrationResponse
import edu.andrews.cas.physics.inventory.server.util.Constants
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
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
        val response = RegistrationResponse(true, true, true, true,true, !usernameTaken, !emailTaken)
        logger.info("[Auth DAO] Registration of user '{}' successful? {}", userRegistration.username, response.isValid)
        if (response.isUnique) {
            val future = CompletableFuture<Boolean>()
            val insertResponse = InsertOneBooleanResponse(future)
            val collection = mongodb.getCollection(AUTH_COLLECTION)
            val userDocument = User()
                .firstName(userRegistration.firstName)
                .lastName(userRegistration.lastName)
                .email(userRegistration.email)
                .username(userRegistration.username)
                .password(userRegistration.password)
                .salt(salt)
                .status(UserStatus.ACTIVE)
                .build()
            collection.insertOne(userDocument).subscribe(insertResponse)
            val ack = future.get()
            if (!ack.equals(true)) throw DatabaseException()
        }
        return response
    }

    private fun registerPreRegisteredUser(userRegistration: UserRegistration, salt: String): RegistrationResponse {
        logger.info("[Auth DAO] Attempting to register a pre-registered user with access code {} and email {}", userRegistration.accessCode, userRegistration.email)
        val users = userDAO.findUserByEmail(userRegistration.email).get()
        if (users.isEmpty() || !users[0].accessCode.equals(userRegistration.accessCode)) throw RegistrationNotFoundException()
        if (users[0].status != UserStatus.PENDING) throw AlreadyRegisteredException()
        if (userDAO.findUserByName(userRegistration.username).get().isNotEmpty()) return RegistrationResponse(true, true, true, true,true,false, true)
        val user = users[0].username(userRegistration.username).password(userRegistration.password).salt(salt).status(UserStatus.ACTIVE).emailVerified(userRegistration.isFromEmailLink)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        collection.replaceOne(eq("email", userRegistration.email), user.build()).subscribe(response)
        val ack = future.get()
        if (!ack.equals(true)) throw DatabaseException()
        return RegistrationResponse(true, true, true, true, true, true, true)
    }

    fun loginAttempt(user: String, valid: Boolean) {
        logger.info("[Auth DAO] Setting login attempt for user: {}\tValid: {}", user, valid)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        val future = CompletableFuture<Document>()
        val response = FindOneAndUpdateResponse(future)
        val now = LocalDateTime.now()
        if (valid) collection.findOneAndUpdate(
            eq("username", user),
            combine(
                set("failedAttempts", 0),
                set("lastAttempt", now),
                set("lastSuccess", now)))
            .subscribe(response)
        else collection.findOneAndUpdate(
            eq("username", user),
            combine(
                inc("failedAttempts", 1),
                set("lastAttempt", now)))
            .subscribe(response)
        // todo fix this
        // this is getting the pre-update document and checking the failed attempts
        // we should be looking to get post-update document for this, or some other alternative
        future.whenCompleteAsync { d, _ ->
            run {
                if (d != null && d.getInteger("failedAttempts") >= Constants.MAX_FAILED_LOGIN_ATTEMPTS - 1) {
                    val future2 = CompletableFuture<Boolean>()
                    val response2 =
                        UpdateBooleanResponse(
                            future2
                        )
                    collection.updateOne(eq("username", user), set("status", UserStatus.LOCKED.name))
                        .subscribe(response2)
                    future2.whenCompleteAsync { _, _ -> }
                }
            }
        }
    }

    fun findUser(username: String): User? {
        logger.info("[Auth DAO] Finding user with username: {}", username)
        val future = userDAO.findUserByName(username)
        val users = future.get()
        return if (users.isNotEmpty()) users[0] else null
    }

    fun setPassword(user: String, password: String, salt: String) {
        logger.info("[Auth DAO] Setting password for user: {}", user)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        collection.updateOne(eq("username", user), combine(set("password", password), set("salt", salt))).subscribe(response)
        future.whenCompleteAsync { _, _ ->  }
    }

    fun unlockAccount(user: String) {
        logger.info("[Auth DAO] Unlocking account for user: {}", user)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(AUTH_COLLECTION)
        collection.updateOne(
            combine(
                eq("username", user),
                eq("status", UserStatus.LOCKED.name)
            ),
            combine(
                set("status", UserStatus.ACTIVE.name),
                set("failedAttempts", 0)
            )
        )
            .subscribe(response)
        future.whenCompleteAsync { _, _ ->  }
    }

    companion object {
        private const val AUTH_COLLECTION: String = "users"
        private val logger: Logger = LogManager.getLogger()
    }
}