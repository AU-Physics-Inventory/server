package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.reactive.FindOneAndUpdateResponse
import edu.andrews.cas.physics.inventory.server.reactive.UpdateBooleanResponse
import edu.andrews.cas.physics.inventory.server.reactive.UserFinder
import edu.andrews.cas.physics.inventory.server.repository.model.User
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class UserDAO @Autowired constructor(private val mongodb: MongoDatabase){
    fun verifyEmail(user: String): String {
        logger.info("[User DAO] Setting e-mail as verified for user: {}", user)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<Document>()
        val updateResponse = FindOneAndUpdateResponse(future)
        collection.findOneAndUpdate(eq("username", user), set("emailVerified", true)).subscribe(updateResponse)
        return future.get().getString("email")
    }

    fun findUserByName(user: String): CompletableFuture<List<User>> {
        logger.info("[User DAO] Retrieving document for user {}", user)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<List<User>>()
        val finder = UserFinder(future)
        collection.find(eq("username", user)).subscribe(finder)
        return future
    }

    fun findUserByEmail(email: String) : CompletableFuture<List<User>> {
        logger.info("[User DAO] Retrieving documents for users with email {}", email)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<List<User>>()
        val finder = UserFinder(future)
        collection.find(eq("email", email)).subscribe(finder)
        return future
    }

    fun updateEmail(user: String, email: String) {
        logger.info("[User DAO] Updating email for user '{}'", user)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<Boolean>()
        val response = UpdateBooleanResponse(future)
        collection.updateOne(eq("username", user), combine(set("email", email), set("emailVerified", false))).subscribe(response)
        future.whenCompleteAsync{ _, _ -> }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val USER_COLLECTION: String = "users"
    }
}