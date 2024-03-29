package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.exception.AlreadyRegisteredException
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.model.UserStatus
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneBooleanResponse
import edu.andrews.cas.physics.inventory.server.reactive.UpdateBooleanResponse
import edu.andrews.cas.physics.inventory.server.reactive.UserFinder
import edu.andrews.cas.physics.inventory.server.repository.model.User
import edu.andrews.cas.physics.inventory.server.request.user.UserRole
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.BsonString
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AdminDAO @Autowired constructor(private val mongodb: MongoDatabase, private val userDAO: UserDAO){
    fun register(user: Document) {
        logger.info("[Admin DAO] Attempting to register user with email {} as pending", user.getString("email"))
        val usersFound = userDAO.findUserByEmail(user.getString("email"))
        if (usersFound.get().isNotEmpty()) throw AlreadyRegisteredException()
        else {
            val collection = mongodb.getCollection(USER_COLLECTION)
            val future = CompletableFuture<Boolean>()
            val response =
                InsertOneBooleanResponse(future)
            collection.insertOne(user).subscribe(response)
            if (!future.get().equals(true)) throw DatabaseException()
        }
    }

    fun addUserRole(userRole: UserRole) {
        logger.info("[Admin DAO] Adding role '{}' to user '{}'", userRole.role, userRole.username)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(USER_COLLECTION)
        collection.updateOne(eq("username", userRole.username), addToSet("roles", userRole.role)).subscribe(response)
        future.whenCompleteAsync { _, _ -> }
    }

    fun removeUserRole(userRole: UserRole) {
        logger.info("[Admin DAO] Removing role '{}' from user '{}'", userRole.role, userRole.username)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(USER_COLLECTION)
        collection.updateOne(eq("username", userRole.username), pull("roles", userRole.role)).subscribe(response)
        future.whenCompleteAsync { _, _ -> }
    }

    fun setUserStatus(username: String, status: UserStatus) {
        logger.info("[Admin DAO] Setting status for user '{}' as '{}'", username, status)
        val future = CompletableFuture<Boolean>()
        val response =
            UpdateBooleanResponse(future)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val statusUpdate = set("status", status.name)
        val update = if (status == UserStatus.ACTIVE) combine(statusUpdate, set("failedAttempts", 0)) else statusUpdate
        collection.updateOne(eq("username", username), update).subscribe(response)
        future.whenCompleteAsync { _, _ -> }
    }

    fun getUsers(role: String?): List<User> {
        logger.info("[Admin DAO] Retrieving list of all users {}", role)
        val future = CompletableFuture<List<User>>()
        val response = UserFinder(future)
        val collection = mongodb.getCollection(USER_COLLECTION)
        if (role == null) collection.find().subscribe(response)
        else collection.find(eq("roles", BsonString(role))).subscribe(response)
        return future.get()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val USER_COLLECTION: String = "users"
    }
}