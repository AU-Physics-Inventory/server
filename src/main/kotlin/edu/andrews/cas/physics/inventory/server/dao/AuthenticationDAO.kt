package edu.andrews.cas.physics.inventory.server.dao

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.*
import com.mongodb.reactivestreams.client.MongoDatabase
import edu.andrews.cas.physics.inventory.server.auth.UserRegistration
import edu.andrews.cas.physics.inventory.server.exception.DatabaseException
import edu.andrews.cas.physics.inventory.server.reactive.DocumentFinder
import edu.andrews.cas.physics.inventory.server.reactive.FindOneAndUpdateResponse
import edu.andrews.cas.physics.inventory.server.reactive.InsertOneResponse
import edu.andrews.cas.physics.inventory.server.reactive.UpdateResponse
import edu.andrews.cas.physics.inventory.server.response.RegistrationResponse
import edu.andrews.cas.physics.inventory.server.service.AuthenticationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Component
open class AuthenticationDAO @Autowired constructor(private val mongodb: MongoDatabase) {
    fun getPepperAndHash(username: String) : Pair<String, String>? {
        logger.info("[Auth DAO] Retrieving pepper and hash for user {}", username)
        val collection = this.mongodb.getCollection(AUTH_COLLECTION)
        val future = CompletableFuture<List<Document>>()
        val documentFinder = DocumentFinder(future)
        collection.find(eq("username", username)).subscribe(documentFinder)
        val documents = future.get()
        return if (documents.isNotEmpty()) Pair(documents[0].getString("salt"), documents[0].getString("password")) else null
    }

    fun registerUser(userRegistration: UserRegistration, pepper: String) : RegistrationResponse {
        logger.info("[Auth DAO] Registering user {}", userRegistration.username)
        val collection = this.mongodb.getCollection(AUTH_COLLECTION)
        val registeredFuture = CompletableFuture<List<Document>>()
        val documentFinder = DocumentFinder(registeredFuture)
        collection.find(or(
                eq("username", userRegistration.username),
                eq("email", userRegistration.email)
            )).subscribe(documentFinder)
        var email = false;
        var username = false;
        val documents = registeredFuture.get()
        if (documents.isEmpty()) {
            val future = CompletableFuture<Boolean>()
            val registrationSubscription =
                InsertOneResponse(future)
            val registrationDocument = Document()
                .append("username", userRegistration.username)
                .append("email", userRegistration.email)
                .append("password", userRegistration.password)
                .append("salt", pepper)
                .append("last_attempt", null)
                .append("last_success", null)
                .append("failed_attempts", 0)
                .append("email_verified", false)
                .append("permissions", ArrayList<String>())
            collection.insertOne(registrationDocument).subscribe(registrationSubscription)
            val ack = future.get()
            if (!ack.equals(true)) throw DatabaseException()
        } else {
            for (doc in documents) {
                if (email && username) break
                if (doc.getString("email").equals(userRegistration.email)) email = true
                if (doc.getString("username").equals(userRegistration.username)) username = true
            }
        }
        val response = RegistrationResponse(username, email)
        logger.info("[Auth DAO] Registration of user '{}' successful? {}", userRegistration.username, response.isSuccess)
        return response
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

    companion object {
        private const val AUTH_COLLECTION: String = "users"
        private val logger: Logger = LogManager.getLogger();
    }
}