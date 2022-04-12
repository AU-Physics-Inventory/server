package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import edu.andrews.cas.physics.inventory.server.reactive.FindOneAndUpdateResponse
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
        collection.findOneAndUpdate(eq("username", user), set("email_verified", true)).subscribe(updateResponse)
        return future.get().getString("email")
    }

    fun findUserByName(user: String): CompletableFuture<List<Document>> {
        logger.info("[User DAO] Retrieving document for user {}", user)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<List<Document>>()
        val finder = DocumentFinder(future)
        collection.find(eq("username", user)).subscribe(finder)
        return future
    }

    fun findUserByEmail(email: String) : CompletableFuture<List<Document>> {
        logger.info("[User DAO] Retrieving documents for users with email {}", email)
        val collection = mongodb.getCollection(USER_COLLECTION)
        val future = CompletableFuture<List<Document>>()
        val finder = DocumentFinder(future)
        collection.find(eq("email", email)).subscribe(finder)
        return future
    }

    companion object {
        private val logger: Logger = LogManager.getLogger()
        private const val USER_COLLECTION: String = "users"
    }
}